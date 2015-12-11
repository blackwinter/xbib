package org.xbib.graphics.barcode.impl.datamatrix;

import java.awt.Dimension;
import java.io.IOException;

import org.xbib.graphics.barcode.TwoDimBarcodeLogicHandler;

/**
 * Top-level class for the logic part of the DataMatrix implementation.
 *
 */
public class DataMatrixLogicImpl {

    /**
     * Generates the barcode logic.
     * @param logic the logic handler to receive generated events
     * @param msg the message to encode
     * @param shape the symbol shape constraint
     * @param minSize the minimum symbol size constraint or null for no constraint
     * @param maxSize the maximum symbol size constraint or null for no constraint
     */
    public void generateBarcodeLogic(TwoDimBarcodeLogicHandler logic, String msg,
            SymbolShapeHint shape, Dimension minSize, Dimension maxSize) {

        //ECC 200
        //1. step: Data encodation
        String encoded;
        try {
            encoded = DataMatrixHighLevelEncoder.encodeHighLevel(msg, shape, minSize, maxSize);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot fetch data: " + e.getLocalizedMessage());
        }

        DataMatrixSymbolInfo symbolInfo = DataMatrixSymbolInfo.lookup(encoded.length(),
                shape, minSize, maxSize, true);

        //2. step: ECC generation
        String codewords = DataMatrixErrorCorrection.encodeECC200(
                encoded, symbolInfo);

        //3. step: Module placement in Matrix
        DefaultDataMatrixPlacement placement = new DefaultDataMatrixPlacement(
                    codewords,
                    symbolInfo.getSymbolDataWidth(), symbolInfo.getSymbolDataHeight());
        placement.place();

        //4. step: low-level encoding
        logic.startBarcode(msg, msg);
        encodeLowLevel(logic, placement, symbolInfo);
        logic.endBarcode();
    }

    private void encodeLowLevel(TwoDimBarcodeLogicHandler logic,
            DataMatrixPlacement placement, DataMatrixSymbolInfo symbolInfo) {
        int symbolWidth = symbolInfo.getSymbolDataWidth();
        int symbolHeight = symbolInfo.getSymbolDataHeight();
        for (int y = 0; y < symbolHeight; y++) {
            if ((y % symbolInfo.matrixHeight) == 0) {
                logic.startRow();
                for (int x = 0; x < symbolInfo.getSymbolWidth(); x++) {
                    logic.addBar((x % 2) == 0, 1);
                }
                logic.endRow();
            }
            logic.startRow();
            for (int x = 0; x < symbolWidth; x++) {
                if ((x % symbolInfo.matrixWidth) == 0) {
                    logic.addBar(true, 1); //left finder edge
                }
                logic.addBar(placement.getBit(x, y), 1);
                if ((x % symbolInfo.matrixWidth) == symbolInfo.matrixWidth - 1) {
                    logic.addBar((y % 2) == 0, 1); //right finder edge
                }
            }
            logic.endRow();
            if ((y % symbolInfo.matrixHeight) == symbolInfo.matrixHeight - 1) {
                logic.startRow();
                for (int x = 0; x < symbolInfo.getSymbolWidth(); x++) {
                    logic.addBar(true, 1);
                }
                logic.endRow();
            }
        }
    }

}
