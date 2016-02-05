package org.xbib.text.language.enums;

import org.xbib.text.language.Subtag;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Enum constants used to validate language tags
 */
public enum Variant {

    _1606NICT(null, null, "frm", "Late Middle French (to 1606)"), _1694ACAD(null, null, "fr", "Early Modern French"), _1901(
            null, null, "de", "Traditional German orthography"), _1994(null, null, new String[]{"sl-rozaj",
            "sl-rozaj-biske",
            "sl-rozaj-njiva",
            "sl-rozaj-osojs",
            "sl-rozaj-solba"},
            "Standardized Resian orthography"), _1996(null, null, "de", "German orthography of 1996"), AREVELA(null, null,
            "hy", "Eastern Armenian"), AREVMDA(null, null, "hy", "Western Armenian"), BAKU1926(null, null,
            new String[]{"az", "ba", "crh", "kk", "krc", "ky", "sah", "tk", "tt", "uz"},
            "Unified Turkic Latin Alphabet (Historical)"), BISKE(null, null, "sl-rozaj",
            "The San Giorgio dialect of Resian", "The Bila dialect of Resian"), BOONT(null, null, "en", "Boontling"), FONIPA(
            null, null, (String) null, "International Phonetic Alphabet"), FONUPA(null, null, (String) null,
            "Uralic Phonetic Alphabet"), LIPAW(null, null, "sl-rozaj", "The Lipovaz dialect of Resian",
            "The Lipovec dialect of Resian"), MONOTON(null, null, "el", "Monotonic Greek"), NEDIS(null, null, "sl",
            "Natisone dialect", "Nadiza dialect"), NJIVA(null, null, "sl-rozaj", "The Gniva dialect of Resian",
            "The Njiva dialect of Resian"), OSOJS(null, null, "sl-rozaj", "The Oseacco dialect of Resian",
            "The Osojane dialect of Resian"), POLYTON(null, null, "el", "Polytonic Greek"), ROZAJ(null, null, "sl",
            "Resian", "Resianic", "Rezijan"), SCOTLAND(null, null, "en", "Scottish Standard English"), SCOUSE(null, null,
            "en", "Scouse"), SOLBA(null, null, "sl-rozaj", "The Stolvizza dialect of Resian",
            "The Solbica dialect of Resian"), TARASK(null, null, "be", "Belarusian in Taraskievica orthography"), VALENCIA(
            null, null, "ca", "Valencian");

    private final String deprecated;
    private final String preferred;
    private final List<String> prefixes;
    private final List<String> descriptions;

    Variant(String dep, String pref, String prefix, String... desc) {
        this(dep, pref, new String[]{prefix}, desc);
    }

    Variant(String dep, String pref, String[] prefixes, String... desc) {
        this.deprecated = dep;
        this.preferred = pref;
        this.prefixes = Arrays.asList(prefixes);
        this.descriptions = Arrays.asList(desc);
    }

    public boolean isDeprecated() {
        return deprecated != null;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public String getPreferredValue() {
        return preferred;
    }

    public Variant getPreferred() {
        return preferred != null ? valueOf(preferred.toUpperCase(Locale.US)) : this;
    }

    public String getPrefix() {
        return prefixes != null && !prefixes.isEmpty() ? prefixes.get(0) : null;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public String getDescription() {
        return descriptions != null && !descriptions.isEmpty() ? descriptions.get(0) : null;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public Subtag newSubtag() {
        return new Subtag(this);
    }

    public static Variant valueOf(Subtag subtag) {
        if (subtag == null) {
            return null;
        }
        if (subtag.getType() == Subtag.Type.VARIANT) {
            String name = subtag.getName();
            if (name.startsWith("1")) {
                name = "_" + name;
            }
            return valueOf(name.toUpperCase(Locale.US));
        } else {
            throw new IllegalArgumentException("Wrong subtag type");
        }
    }

}
