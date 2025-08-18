/** Defines the interface for Printer plugin */
export interface PrinterPlugin {
  /** 
   * Initialize the printer connection
   * @returns {Promise<{success: boolean}>} A promise that resolves when initialization is complete
   */
  initialize(): Promise<{ success: boolean }>;
  
  /**
   * Print text to the connected printer
   * @param {PrintOptions} options - Options for printing text
   * @returns {Promise<{success: boolean}>} A promise that resolves when printing is complete
   */
  printText(options: PrintOptions): Promise<{ success: boolean }>;
  
  /**
   * Print a barcode
   * @param {BarcodeOptions} options - Options for printing barcode
   * @returns {Promise<{success: boolean}>} A promise that resolves when barcode printing is complete
   */
  printBarcode(options: BarcodeOptions): Promise<{ success: boolean }>;
  
  /**
   * Check printer status
   * @returns {Promise<PrinterStatus>} A promise that resolves to the printer status
   */
  getStatus(): Promise<PrinterStatus>;
  
  /**
   * Disconnect from the printer
   * @returns {Promise<{success: boolean}>} A promise that resolves when disconnection is complete
   */
  disconnect(): Promise<{ success: boolean }>;
  
  /**
   * Discover network printers on the network
   * @param {DiscoveryOptions} options - Options for network discovery
   * @returns {Promise<DiscoveryResult>} A promise that resolves with discovered printers
   */
  discoverNetworkPrinters(options?: DiscoveryOptions): Promise<DiscoveryResult>;
  
  /**
   * Connect to a specific printer
   * @param {ConnectOptions} options - Options for connecting to printer
   * @returns {Promise<{success: boolean, message?: string}>} A promise that resolves when connection is complete
   */
  connect(options: ConnectOptions): Promise<{ success: boolean; message?: string }>;
  
  /**
   * Print a PDF document
   * @param {PDFOptions} options - Options for printing PDF
   * @returns {Promise<{success: boolean}>} A promise that resolves when PDF printing is complete
   */
  printPDF(options: PDFOptions): Promise<{ success: boolean }>;
  
  /**
   * Check if the printer instance is initialized
   * @returns {Promise<{initialized: boolean}>} A promise that resolves with initialization status
   */
  isInitialized(): Promise<{ initialized: boolean }>;
}

/** Options for printing text */
export interface PrintOptions {
  /** Text to print */
  text: string;
  /** Font size (optional, defaults to normal) */
  fontSize?: 'small' | 'normal' | 'medium' | 'large' | 'xlarge';
  /** Text alignment (optional, defaults to left) */
  alignment?: 'left' | 'center' | 'right';
  /** Whether to use bold text (optional, defaults to false) */
  bold?: boolean;
  /** Horizontal position (optional) */
  horizontalPosition?: number;
  /** Vertical position (optional) */
  verticalPosition?: number;
}

/** Options for printing barcodes */
export interface BarcodeOptions {
  /** Barcode data */
  data: string;
  /** Barcode type */
  barcodeType?: 'CODE128' | 'CODE39' | 'CODE93' | 'CODABAR' | 'ITF' | 'UPC_A' | 'UPC_E' | 'EAN13' | 'EAN8';
  /** Width of barcode (optional) */
  width?: number;
  /** Height of barcode (optional) */
  height?: number;
  /** Horizontal position (optional) */
  horizontalPosition?: number;
  /** Vertical position (optional) */
  verticalPosition?: number;
  /** Show human readable interpretation (optional) */
  hri?: boolean;
}

/** Options for network printer discovery */
export interface DiscoveryOptions {
  /** Discovery timeout in milliseconds (optional, defaults to 5000) */
  timeout?: number;
}

/** Result of network printer discovery */
export interface DiscoveryResult {
  /** Whether discovery was successful */
  success: boolean;
  /** Array of discovered printer IP addresses/hostnames */
  devices: string[];
}

/** Options for connecting to a printer */
export interface ConnectOptions {
  /** Printer address (IP address for network printers) */
  address: string;
  /** Connection type (optional, defaults to network) */
  type?: 'network' | 'bluetooth' | 'usb';
  /** Port number for network connections (optional, defaults to 9100) */
  port?: number;
  /** Connection timeout in milliseconds (optional, defaults to 5000) */
  timeout?: number;
}

/** Options for printing PDF */
export interface PDFOptions {
  /** Base64 encoded PDF file string */
  base64FileString: string;
  /** Width for PDF rendering (optional, defaults to 576) */
  width?: number;
  /** Horizontal position (optional, defaults to 0) */
  horizontalPosition?: number;
  /** Vertical position (optional, defaults to 0) */
  verticalPosition?: number;
  /** Page number to print (optional, defaults to 1) */
  page?: number;
  /** Enable dithering (optional, defaults to true) */
  dithering?: boolean;
  /** Enable compression (optional, defaults to true) */
  compress?: boolean;
  /** Quality level (optional, defaults to 0) */
  level?: number;
}

/** Printer status information */
export interface PrinterStatus {
  /** Whether printer is connected */
  connected: boolean;
  /** Whether printer is ready to print */
  ready: boolean;
  /** Paper status */
  paperStatus: 'ok' | 'out' | 'cover_open' | 'unknown' | 'disconnected';
  /** Whether paper is out (optional) */
  paperOut?: boolean;
  /** Whether cover is open (optional) */
  coverOpen?: boolean;
}