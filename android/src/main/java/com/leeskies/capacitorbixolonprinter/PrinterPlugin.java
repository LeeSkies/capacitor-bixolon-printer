package com.leeskies.capacitorbixolonprinter;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;;

@CapacitorPlugin(name = "BixolonPrinter")
public class PrinterPlugin extends Plugin {

    private Printer implementation;

    @Override
    public void load() {
        // Don't initialize here - will be done per method call
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        try {
            implementation = new Printer(this.getContext(), call);
            JSObject response = new JSObject();
            response.put("success", true);
            call.resolve(response);
        } catch (Exception e) {
            JSObject response = new JSObject();
            response.put("success", false);
            call.reject("Initialization failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void printText(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.printText(call);
    }

    @PluginMethod
    public void printBarcode(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.printBarcode(call);
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.getStatus(call);
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.disconnect(call);
    }

    @PluginMethod
    public void discoverNetworkPrinters(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.discoverNetworkPrinters(call);
    }

    @PluginMethod
    public void connect(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.connect(call);
    }

    @PluginMethod
    public void printPDF(PluginCall call) {
        if (implementation == null) {
            call.reject("Printer not initialized. Call initialize() first.");
            return;
        }
        implementation.printPDF(call);
    }
}