import { registerPlugin } from '@capacitor/core';

import type { PrinterPlugin } from './definitions';

const Printer = registerPlugin<PrinterPlugin>('BixolonPrinter');

export * from './definitions';
export { Printer };