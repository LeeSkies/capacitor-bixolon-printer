import { WebPlugin } from '@capacitor/core';

import type { PrinterPlugin, PrintOptions, BarcodeOptions, PrinterStatus, DiscoveryOptions, DiscoveryResult, ConnectOptions, PDFOptions } from './definitions';

export class PrinterWeb extends WebPlugin implements PrinterPlugin {
  async initialize(): Promise<{ success: boolean }> {
    console.log('Printer web implementation: initialize not supported on web');
    return { success: false };
  }

  async printText(options: PrintOptions): Promise<{ success: boolean }> {
    console.log('Printer web implementation: printText', options);
    return { success: false };
  }

  async printBarcode(options: BarcodeOptions): Promise<{ success: boolean }> {
    console.log('Printer web implementation: printBarcode', options);
    return { success: false };
  }

  async getStatus(): Promise<PrinterStatus> {
    console.log('Printer web implementation: getStatus');
    return {
      connected: false,
      ready: false,
      paperStatus: 'ok'
    };
  }

  async disconnect(): Promise<{ success: boolean }> {
    console.log('Printer web implementation: disconnect');
    return { success: false };
  }

  async discoverNetworkPrinters(options?: DiscoveryOptions): Promise<DiscoveryResult> {
    console.log('Printer web implementation: discoverNetworkPrinters', options);
    return { success: false, devices: [] };
  }

  async connect(options: ConnectOptions): Promise<{ success: boolean; message?: string }> {
    console.log('Printer web implementation: connect', options);
    return { success: false, message: 'Not supported on web' };
  }

  async printPDF(options: PDFOptions): Promise<{ success: boolean }> {
    console.log('Printer web implementation: printPDF', options);
    return { success: false };
  }

  async isInitialized(): Promise<{ initialized: boolean }> {
    console.log('Printer web implementation: isInitialized');
    return { initialized: false };
  }
}