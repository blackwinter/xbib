package org.xbib.graphics.barcode.impl.fourstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xbib.graphics.barcode.ChecksumMode;

/**
 * Abstract base class for Royal Mail Customer Barcode and the Dutch KIX Code.
 */
public abstract class AbstractRMCBCKIXLogicImpl extends AbstractFourStateLogicImpl {

    private static final Map<String,String> CHARSET = new HashMap<>();
    
    static {
        //0 = track only, 1 = ascender, 2 = descender, 3 = 1 + 2 = full height
        CHARSET.put("(", "1");
        CHARSET.put("[", "1");
        CHARSET.put(")", "3");
        CHARSET.put("]", "3");
        CHARSET.put("0", "0033");
        CHARSET.put("1", "0213");
        CHARSET.put("2", "0231");
        CHARSET.put("3", "2013");
        CHARSET.put("4", "2031");
        CHARSET.put("5", "2211");
        CHARSET.put("6", "0123");
        CHARSET.put("7", "0303");
        CHARSET.put("8", "0321");
        CHARSET.put("9", "2103");
        CHARSET.put("A", "2121");
        CHARSET.put("B", "2301");
        CHARSET.put("C", "0132");
        CHARSET.put("D", "0312");
        CHARSET.put("E", "0330");
        CHARSET.put("F", "2112");
        CHARSET.put("G", "2130");
        CHARSET.put("H", "2310");
        CHARSET.put("I", "1023");
        CHARSET.put("J", "1203");
        CHARSET.put("K", "1221");
        CHARSET.put("L", "3003");
        CHARSET.put("M", "3021");
        CHARSET.put("N", "3201");
        CHARSET.put("O", "1032");
        CHARSET.put("P", "1212");
        CHARSET.put("Q", "1230");
        CHARSET.put("R", "3012");
        CHARSET.put("S", "3030");
        CHARSET.put("T", "3210");
        CHARSET.put("U", "1122");
        CHARSET.put("V", "1302");
        CHARSET.put("W", "1320");
        CHARSET.put("X", "3102");
        CHARSET.put("Y", "3120");
        CHARSET.put("Z", "3300");
    }
    
    public AbstractRMCBCKIXLogicImpl(ChecksumMode mode) {
        super(mode);
    }

    protected String[] encodeHighLevel(String msg) {
        List<String> codewords = new ArrayList<>(msg.length());
        for (int i = 0, c = msg.length(); i < c; i++) {
            String ch = msg.substring(i, i + 1);
            String code = CHARSET.get(ch);
            if (code == null) {
                throw new IllegalArgumentException("Illegal character: " + ch);
            }
            codewords.add(code);
        }
        return codewords.toArray(new String[codewords.size()]);
    }



}
