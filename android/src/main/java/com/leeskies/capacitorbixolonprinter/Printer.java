package com.leeskies.capacitorbixolonprinter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import com.bixolon.labelprinter.BixolonLabelPrinter;
import com.bixolon.labelprinter.PrinterControl;

import java.util.Set;
import java.util.logging.LogManager;

import static com.leeskies.capacitorbixolonprinter.Constants.TAG;

import org.jetbrains.annotations.NotNull;

public class Printer {
    private final Handler mHandler = createHandler();
    
    private Handler createHandler() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
            Log.i(TAG, "myLooper call failed, defaulting to main looper");
            looper = Looper.getMainLooper();
        }
        return new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BixolonLabelPrinter.MESSAGE_NETWORK_DEVICE_SET:
                        handleNetworkDeviceSet(msg);
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };
    }
    
    BixolonLabelPrinter printer;
    private Context context;
    private PluginCall call;
    private PluginCall pendingDiscoveryCall;

    public Printer(Context context, PluginCall call) {
        this.context = context;
        this.call = call;
        Looper looper = Looper.myLooper();
        if (looper == null) {
            Log.e(TAG, "Initialization failed: myLooper returned null");
            call.reject("Failed to initialize printer instance");
            return;
        }
        try {
            printer = new BixolonLabelPrinter(this.context, mHandler, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create BixolonLabelPrinter: " + e.getMessage());
            call.reject("Failed to initialize printer: " + e.getMessage());
        }
    }

    public void discoverNetworkPrinters(PluginCall call) {
        try {
            int timeout = call.getInt("timeout", 5000);
            this.pendingDiscoveryCall = call;
            this.printer.findNetworkPrinters(timeout);
            
            // Add fallback timeout to prevent hanging calls
            mHandler.postDelayed(() -> {
                if (pendingDiscoveryCall != null) {
                    Log.w(TAG, "Discovery timed out, no response from printer");
                    JSObject response = new JSObject();
                    response.put("success", true);
                    response.put("devices", new JSArray());
                    pendingDiscoveryCall.resolve(response);
                    pendingDiscoveryCall = null;
                }
            }, timeout + 1000); // Add 1 second buffer to SDK timeout
            
        } catch (Exception e) {
            this.pendingDiscoveryCall = null; // Clear pending call on exception
            call.reject("Discovery failed: " + e.getMessage());
        }
    }

    private void handleNetworkDeviceSet(Message msg) {
        if (pendingDiscoveryCall == null) {
            Log.w(TAG, "Received network device set but no pending call");
            return;
        }

        JSObject response = new JSObject();
        try {
            if (msg.obj == null) {
                Log.i(TAG, "Network Device not found!");
                response.put("success", true);
                response.put("devices", new JSArray());
            } else {
                Set<String> devices = (Set<String>) msg.obj;
                JSArray deviceArray = new JSArray();
                for (String device : devices) {
                    deviceArray.put(device);
                }
                response.put("success", true);
                response.put("devices", deviceArray);
                Log.i(TAG, "Found " + devices.size() + " network devices");
            }
            pendingDiscoveryCall.resolve(response);
        } catch (Exception e) {
            Log.e(TAG, "Error handling network device set: " + e.getMessage());
            pendingDiscoveryCall.reject("Failed to process discovered devices: " + e.getMessage());
        } finally {
            pendingDiscoveryCall = null;
        }
    }

    public void printText(PluginCall call) {
        String text = call.getString("text");
        String fontSize = call.getString("fontSize", "normal");
        String alignment = call.getString("alignment", "left");
        Boolean bold = call.getBoolean("bold", false);
        int horizontalPosition = call.getInt("horizontalPosition", 0);
        int verticalPosition = call.getInt("verticalPosition", 0);

        JSObject response = new JSObject();
        try {
            if (text == null || text.isEmpty()) {
                response.put("success", false);
                call.reject("Text cannot be null or empty");
                return;
            }

            if (!printer.isConnected()) {
                response.put("success", false);
                call.reject("Printer is not connected");
                return;
            }

            // Convert fontSize string to integer
            int fontSizeInt = convertFontSize(fontSize);
            
            // Convert alignment string to integer
            int alignmentInt = convertAlignment(alignment);
            
            // Clear buffer before starting new transaction
            printer.clearBuffer();
            printer.beginTransactionPrint();
            int result = printer.drawText(
                text,
                horizontalPosition,
                verticalPosition,
                fontSizeInt,
                1, // horizontalMultiplier
                1, // verticalMultiplier
                0, // rightSpace
                BixolonLabelPrinter.ROTATION_NONE,
                false, // reverse
                bold,
                alignmentInt
            );
            
            if (result == 0) {
                // End transaction and print
                int printResult = printer.endTransactionPrint();
                printer.print(1, 1);
                if (printResult == 3) { // Success code for endTransactionPrint
                    response.put("success", true);
                    call.resolve(response);
                } else {
                    response.put("success", false);
                    call.reject("Failed to print: error code " + printResult);
                }
            } else {
                printer.clearBuffer(); // Clear on failure
                response.put("success", false);
                call.reject("Failed to draw text: error code " + result);
            }
        } catch (Exception e) {
            printer.clearBuffer(); // Clear on exception
            response.put("success", false);
            call.reject("Print text failed: " + e.getMessage());
        }
    }

    public void printPDF(PluginCall call) {
        String base64FileString = call.getString("base64FileString");
        int width = call.getInt("width", 0);
        int horizontalPosition = call.getInt("horizontalPosition", 0);
        int verticalPosition = call.getInt("verticalPosition", 0);
        int page = call.getInt("page", 1);
        Boolean dithering = call.getBoolean("dithering", true);
        Boolean compress = call.getBoolean("compress", true);
        int level = call.getInt("level", 1);
        
        JSObject response = new JSObject();
        File tempFile = null;

        Log.d(TAG, "Printing with params: " + width + ", " + horizontalPosition + ", " + verticalPosition + ", " + page + ", " + dithering + ", " + compress + ", " + level);
        
        try {
            if (base64FileString == null || base64FileString.isEmpty()) {
                response.put("success", false);
                call.reject("Base64 file string cannot be null or empty");
                return;
            }

            if (!printer.isConnected()) {
                response.put("success", false);
                call.reject("Printer is not connected");
                return;
            }

            // Decode base64 string to bytes
            byte[] pdfBytes = Base64.decode(base64FileString, Base64.DEFAULT);
            
            // Create temporary file
            tempFile = File.createTempFile("temp_pdf", ".pdf", context.getCacheDir());
            
            // Write PDF bytes to temporary file
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
                fos.flush();
            }
            
            // Create URI from temporary file
            Uri pdfUri = Uri.fromFile(tempFile);
            
            // Clear buffer and begin transaction for printing
            printer.clearBuffer();
            printer.beginTransactionPrint();
            
            // Draw PDF file
            Log.d(TAG, "Drawing PDF file with parameters: " + pdfUri + ", " + horizontalPosition + ", " + verticalPosition + ", " + page + ", " + width + ", " + level + ", " + dithering + ", " + compress);

            int result = printer.drawPDFFile(
                pdfUri,
                horizontalPosition,
                verticalPosition,
                page,
                width,
                level,
                dithering,
                compress
            );

            Log.d(TAG, "drawPDFFile result: " + result);
            
            if (result == 0) {
                // End transaction and print
                int printResult = printer.endTransactionPrint();
                printer.print(1, 1);
                if (printResult == 3) { // Success code for endTransactionPrint
                    response.put("success", true);
                    call.resolve(response);
                } else {
                    printer.clearBuffer(); // Clear on failure
                    response.put("success", false);
                    call.reject("Failed to print PDF: error code " + printResult);
                }
            } else {
                printer.clearBuffer(); // Clear on failure
                response.put("success", false);
                call.reject("Failed to draw PDF: error code " + result);
            }
        } catch (IOException e) {
            printer.clearBuffer(); // Clear on exception
            response.put("success", false);
            call.reject("Failed to create temporary PDF file: " + e.getMessage());
        } catch (Exception e) {
            printer.clearBuffer(); // Clear on exception
            response.put("success", false);
            call.reject("Print PprintpdfDF failed: " + e.getMessage());
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public void getStatus(PluginCall call) {
        JSObject response = new JSObject();
        try {
            boolean connected = printer.isConnected();
            response.put("connected", connected);
            
            if (connected) {
                // Get printer status
                byte[] statusBytes = printer.getStatus(false);
                
                if (statusBytes != null && statusBytes.length > 0) {
                    // Parse status bytes to determine printer state
                    // First byte typically contains general status information
                    byte generalStatus = statusBytes[0];
                    
                    // Check if printer is ready (no errors)
                    boolean ready = (generalStatus & 0x08) == 0; // Bit 3: 0 = ready, 1 = error
                    boolean paperOut = (generalStatus & 0x04) != 0; // Bit 2: paper out
                    boolean coverOpen = (generalStatus & 0x20) != 0; // Bit 5: cover open
                    
                    response.put("ready", ready);
                    response.put("paperOut", paperOut);
                    response.put("coverOpen", coverOpen);
                    
                    if (paperOut) {
                        response.put("paperStatus", "out");
                    } else if (coverOpen) {
                        response.put("paperStatus", "cover_open");
                    } else {
                        response.put("paperStatus", "ok");
                    }
                } else {
                    response.put("ready", false);
                    response.put("paperStatus", "unknown");
                }
            } else {
                response.put("ready", false);
                response.put("paperStatus", "disconnected");
            }
            
            call.resolve(response);
        } catch (Exception e) {
            call.reject("Get status failed: " + e.getMessage());
        }
    }

    public void disconnect(PluginCall call) {
        JSObject response = new JSObject();
        try {
            if (printer != null) {
                printer.disconnect();
                response.put("success", true);
                Log.i(TAG, "Printer disconnected successfully");
            } else {
                response.put("success", false);
                Log.w(TAG, "Printer instance is null, cannot disconnect");
            }
            call.resolve(response);
        } catch (Exception e) {
            response.put("success", false);
            Log.e(TAG, "Disconnect failed: " + e.getMessage());
            call.reject("Disconnect failed: " + e.getMessage());
        }
    }

    public void printBarcode(PluginCall call) {
        String data = call.getString("data");
        String barcodeType = call.getString("barcodeType", "CODE128");
        int width = call.getInt("width", 2);
        int height = call.getInt("height", 100);
        int horizontalPosition = call.getInt("horizontalPosition", 0);
        int verticalPosition = call.getInt("verticalPosition", 0);
        Boolean hri = call.getBoolean("hri", false); // Human Readable Interpretation

        JSObject response = new JSObject();
        try {
            if (data == null || data.isEmpty()) {
                response.put("success", false);
                call.reject("Barcode data cannot be null or empty");
                return;
            }

            if (!printer.isConnected()) {
                response.put("success", false);
                call.reject("Printer is not connected");
                return;
            }

            // Clear buffer and begin transaction for printing
            printer.clearBuffer();
            printer.beginTransactionPrint();
            
            // Convert barcode type string to integer
            int barcodeSelection = convertBarcodeType(barcodeType);
            
            // Draw 1D barcode
            int result = printer.draw1dBarcode(
                data,
                horizontalPosition,
                verticalPosition,
                barcodeSelection,
                width, // narrowBarWidth
                width * 2, // wideBarWidth
                height,
                BixolonLabelPrinter.ROTATION_NONE,
                hri ? BixolonLabelPrinter.HRI_BELOW_BARCODE : BixolonLabelPrinter.HRI_NOT_PRINTED,
                0 // quietZoneWidth
            );
            
            if (result == 0) {
                // End transaction and print
                int printResult = printer.endTransactionPrint();
                printer.print(1, 1);
                if (printResult == 3) { // Success code for endTransactionPrint
                    response.put("success", true);
                    call.resolve(response);
                } else {
                    response.put("success", false);
                    call.reject("Failed to print barcode: error code " + printResult);
                }
            } else {
                printer.clearBuffer(); // Clear on failure
                response.put("success", false);
                call.reject("Failed to draw barcode: error code " + result);
            }
        } catch (Exception e) {
            printer.clearBuffer(); // Clear on exception
            response.put("success", false);
            call.reject("Print barcode failed: " + e.getMessage());
        }
    }

    public void connect(PluginCall call) {
        String address = call.getString("address");
        String type = call.getString("type", "network");
        int port = call.getInt("port", 9100);
        int timeout = call.getInt("timeout", 5000);

        JSObject response = new JSObject();
        try {
            if (address == null || address.isEmpty()) {
                response.put("success", false);
                call.reject("Address cannot be null or empty");
                return;
            }

            String result;
            if ("network".equals(type)) {
                Log.d(TAG, "Connecting to network printer at " + address + ":" + port + " with timeout of " + timeout + "ms");
                result = printer.connect(address, port, timeout);
                Log.d(TAG, "Connection result: " + result);
            } else {
                result = printer.connect(address);
            }

            // Check if connection was successful
            // Success: non-null result that doesn't contain error codes
            if (result != null && !result.trim().isEmpty() && !result.contains("FAIL") && !result.contains("ERROR")) {
                response.put("success", true);
                response.put("message", "Connected to: " + result);
                response.put("printerName", result);
                Log.i(TAG, "Printer connected successfully, printer name: " + result);
                call.resolve(response);
            } else {
                response.put("success", false);
                response.put("message", "Connection failed: " + (result != null ? result : "null"));
                Log.e(TAG, "Connection failed with result: " + result);
                call.reject("Connection failed: " + (result != null ? result : "Unknown error"));
            }
        } catch (Exception e) {
            response.put("success", false);
            call.reject("Connection failed: " + e.getMessage());
        }
    }

    public void isInitialized(PluginCall call) {
        JSObject response = new JSObject();
        try {
            boolean initialized = printer != null;
            response.put("initialized", initialized);
            call.resolve(response);
        } catch (Exception e) {
            call.reject("Failed to check initialization status: " + e.getMessage());
        }
    }

    private int convertFontSize(String fontSize) {
        switch (fontSize.toLowerCase()) {
            case "small":
                return BixolonLabelPrinter.FONT_SIZE_8;
            case "normal":
            case "medium":
                return BixolonLabelPrinter.FONT_SIZE_12;
            case "large":
                return BixolonLabelPrinter.FONT_SIZE_20;
            case "xlarge":
                return BixolonLabelPrinter.FONT_SIZE_30;
            default:
                return BixolonLabelPrinter.FONT_SIZE_12;
        }
    }

    private int convertAlignment(String alignment) {
        switch (alignment.toLowerCase()) {
            case "left":
                return BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT;
            case "center":
                return BixolonLabelPrinter.VECTOR_FONT_TEXT_ALIGNMENT_CENTER;
            case "right":
                return BixolonLabelPrinter.TEXT_ALIGNMENT_RIGHT;
            default:
                return BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT; // default to left
        }
    }

    private int convertBarcodeType(String barcodeType) {
        switch (barcodeType.toUpperCase()) {
            case "CODE39":
                return BixolonLabelPrinter.BARCODE_CODE39;
            case "CODE93":
                return BixolonLabelPrinter.BARCODE_CODE93;
            case "CODE128":
                return BixolonLabelPrinter.BARCODE_CODE128;
            case "CODABAR":
                return BixolonLabelPrinter.BARCODE_CODABAR;
            case "ITF":
                return BixolonLabelPrinter.BARCODE_I2OF5;
            case "UPC_A":
                return BixolonLabelPrinter.BARCODE_UPC_A;
            case "UPC_E":
                return BixolonLabelPrinter.BARCODE_UPC_E;
            case "EAN13":
                return BixolonLabelPrinter.BARCODE_EAN13;
            case "EAN8":
                return BixolonLabelPrinter.BARCODE_EAN8;
            default:
                return BixolonLabelPrinter.BARCODE_CODE128; // default to CODE128
        }
    }
}