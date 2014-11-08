package com.intellectualcrafters.json;

/**
 * Kim makes immutable eight bit Unicode strings. If the MSB of a byte is set,
 * then the next byte is a continuation byte. The last byte of a character
 * never has the MSB reset. Every byte that is not the last byte has the MSB
 * set. Kim stands for "Keep it minimal". A Unicode character is never longer
 * than 3 bytes. Every byte contributes 7 bits to the character. ASCII is
 * unmodified.
 * <p/>
 * Kim UTF-8
 * one byte U+007F U+007F
 * two bytes U+3FFF U+07FF
 * three bytes U+10FFF U+FFFF
 * four bytes U+10FFFF
 * <p/>
 * Characters in the ranges U+0800..U+3FFF and U+10000..U+10FFFF will be one
 * byte smaller when encoded in Kim compared to UTF-8.
 * <p/>
 * Kim is beneficial when using scripts such as Old South Arabian, Aramaic,
 * Avestan, Balinese, Batak, Bopomofo, Buginese, Buhid, Carian, Cherokee,
 * Coptic, Cyrillic, Deseret, Egyptian Hieroglyphs, Ethiopic, Georgian,
 * Glagolitic, Gothic, Hangul Jamo, Hanunoo, Hiragana, Kanbun, Kaithi,
 * Kannada, Katakana, Kharoshthi, Khmer, Lao, Lepcha, Limbu, Lycian, Lydian,
 * Malayalam, Mandaic, Meroitic, Miao, Mongolian, Myanmar, New Tai Lue,
 * Ol Chiki, Old Turkic, Oriya, Osmanya, Pahlavi, Parthian, Phags-Pa,
 * Phoenician, Samaritan, Sharada, Sinhala, Sora Sompeng, Tagalog, Tagbanwa,
 * Takri, Tai Le, Tai Tham, Tamil, Telugu, Thai, Tibetan, Tifinagh, UCAS.
 * <p/>
 * A kim object can be constructed from an ordinary UTF-16 string, or from a
 * byte array. A kim object can produce a UTF-16 string.
 * <p/>
 * As with UTF-8, it is possible to detect character boundaries within a byte
 * sequence. UTF-8 is one of the world's great inventions. While Kim is more
 * efficient, it is not clear that it is worth the expense of transition.
 *
 * @version 2013-04-18
 */
public class Kim {

    /**
     * The number of bytes in the kim. The number of bytes can be as much as
     * three times the number of characters.
     */
    public int length = 0;
    /**
     * The byte array containing the kim's content.
     */
    private byte[] bytes = null;
    /**
     * The kim's hashcode, conforming to Java's hashcode conventions.
     */
    private int hashcode = 0;
    /**
     * The memoization of toString().
     */
    private String string = null;

    /**
     * Make a kim from a portion of a byte array.
     *
     * @param bytes A byte array.
     * @param from  The index of the first byte.
     * @param thru  The index of the last byte plus one.
     */
    public Kim(final byte[] bytes, final int from, final int thru) {

        // As the bytes are copied into the new kim, a hashcode is computed
        // using a
        // modified Fletcher code.

        int sum = 1;
        int value;
        this.hashcode = 0;
        this.length = thru - from;
        if (this.length > 0) {
            this.bytes = new byte[this.length];
            for (int at = 0; at < this.length; at += 1) {
                value = bytes[at + from] & 0xFF;
                sum += value;
                this.hashcode += sum;
                this.bytes[at] = (byte) value;
            }
            this.hashcode += sum << 16;
        }
    }

    /**
     * Make a kim from a byte array.
     *
     * @param bytes  The byte array.
     * @param length The number of bytes.
     */
    public Kim(final byte[] bytes, final int length) {
        this(bytes, 0, length);
    }

    /**
     * Make a new kim from a substring of an existing kim. The coordinates are
     * in byte units, not character units.
     *
     * @param kim  The source of bytes.
     * @param from The point at which to take bytes.
     * @param thru The point at which to stop taking bytes.
     */
    public Kim(final Kim kim, final int from, final int thru) {
        this(kim.bytes, from, thru);
    }

    /**
     * Make a kim from a string.
     *
     * @param string The string.
     * @throws JSONException if surrogate pair mismatch.
     */
    public Kim(final String string) throws JSONException {
        final int stringLength = string.length();
        this.hashcode = 0;
        this.length = 0;

        // First pass: Determine the length of the kim, allowing for the UTF-16
        // to UTF-32 conversion, and then the UTF-32 to Kim conversion.

        if (stringLength > 0) {
            for (int i = 0; i < stringLength; i += 1) {
                final int c = string.charAt(i);
                if (c <= 0x7F) {
                    this.length += 1;
                } else if (c <= 0x3FFF) {
                    this.length += 2;
                } else {
                    if ((c >= 0xD800) && (c <= 0xDFFF)) {
                        i += 1;
                        final int d = string.charAt(i);
                        if ((c > 0xDBFF) || (d < 0xDC00) || (d > 0xDFFF)) {
                            throw new JSONException("Bad UTF16");
                        }
                    }
                    this.length += 3;
                }
            }

            // Second pass: Allocate a byte array and fill that array with the
            // conversion
            // while computing the hashcode.

            this.bytes = new byte[this.length];
            int at = 0;
            int b;
            int sum = 1;
            for (int i = 0; i < stringLength; i += 1) {
                int character = string.charAt(i);
                if (character <= 0x7F) {
                    this.bytes[at] = (byte) character;
                    sum += character;
                    this.hashcode += sum;
                    at += 1;
                } else if (character <= 0x3FFF) {
                    b = 0x80 | (character >>> 7);
                    this.bytes[at] = (byte) b;
                    sum += b;
                    this.hashcode += sum;
                    at += 1;
                    b = character & 0x7F;
                    this.bytes[at] = (byte) b;
                    sum += b;
                    this.hashcode += sum;
                    at += 1;
                } else {
                    if ((character >= 0xD800) && (character <= 0xDBFF)) {
                        i += 1;
                        character = (((character & 0x3FF) << 10) | (string.charAt(i) & 0x3FF)) + 65536;
                    }
                    b = 0x80 | (character >>> 14);
                    this.bytes[at] = (byte) b;
                    sum += b;
                    this.hashcode += sum;
                    at += 1;
                    b = 0x80 | ((character >>> 7) & 0xFF);
                    this.bytes[at] = (byte) b;
                    sum += b;
                    this.hashcode += sum;
                    at += 1;
                    b = character & 0x7F;
                    this.bytes[at] = (byte) b;
                    sum += b;
                    this.hashcode += sum;
                    at += 1;
                }
            }
            this.hashcode += sum << 16;
        }
    }

    /**
     * Returns the number of bytes needed to contain the character in Kim
     * format.
     *
     * @param character a Unicode character between 0 and 0x10FFFF.
     * @return 1, 2, or 3
     * @throws JSONException if the character is not representable in a kim.
     */
    public static int characterSize(final int character) throws JSONException {
        if ((character < 0) || (character > 0x10FFFF)) {
            throw new JSONException("Bad character " + character);
        }
        return character <= 0x7F ? 1 : character <= 0x3FFF ? 2 : 3;
    }

    /**
     * Returns the character at the specified index. The index refers to byte
     * values and ranges from 0 to length - 1. The index of the next character
     * is at index + Kim.characterSize(kim.characterAt(index)).
     *
     * @param at the index of the char value. The first character is at 0.
     * @throws JSONException if at does not point to a valid character.
     * @returns a Unicode character between 0 and 0x10FFFF.
     */
    public int characterAt(final int at) throws JSONException {
        final int c = get(at);
        if ((c & 0x80) == 0) {
            return c;
        }
        int character;
        final int c1 = get(at + 1);
        if ((c1 & 0x80) == 0) {
            character = ((c & 0x7F) << 7) | c1;
            if (character > 0x7F) {
                return character;
            }
        } else {
            final int c2 = get(at + 2);
            character = ((c & 0x7F) << 14) | ((c1 & 0x7F) << 7) | c2;
            if (((c2 & 0x80) == 0) && (character > 0x3FFF) && (character <= 0x10FFFF) && ((character < 0xD800) || (character > 0xDFFF))) {
                return character;
            }
        }
        throw new JSONException("Bad character at " + at);
    }

    /**
     * Copy the contents of this kim to a byte array.
     *
     * @param bytes A byte array of sufficient size.
     * @param at    The position within the byte array to take the byes.
     * @return The position immediately after the copy.
     */
    public int copy(final byte[] bytes, final int at) {
        System.arraycopy(this.bytes, 0, bytes, at, this.length);
        return at + this.length;
    }

    /**
     * Two kim objects containing exactly the same bytes in the same order are
     * equal to each other.
     *
     * @param obj the other kim with which to compare.
     * @returns true if this and obj are both kim objects containing identical
     * byte sequences.
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Kim)) {
            return false;
        }
        final Kim that = (Kim) obj;
        if (this == that) {
            return true;
        }
        if (this.hashcode != that.hashcode) {
            return false;
        }
        return java.util.Arrays.equals(this.bytes, that.bytes);
    }

    /**
     * Get a byte from a kim.
     *
     * @param at The position of the byte. The first byte is at 0.
     * @return The byte.
     * @throws JSONException if there is no byte at that position.
     */
    public int get(final int at) throws JSONException {
        if ((at < 0) || (at > this.length)) {
            throw new JSONException("Bad character at " + at);
        }
        return (this.bytes[at]) & 0xFF;
    }

    /**
     * Returns a hash code value for the kim.
     */
    @Override
    public int hashCode() {
        return this.hashcode;
    }

    /**
     * Produce a UTF-16 String from this kim. The number of codepoints in the
     * string will not be greater than the number of bytes in the kim, although
     * it could be less.
     *
     * @return The string. A kim memoizes its string representation.
     * @throws JSONException if the kim is not valid.
     */
    @Override
    public String toString() throws JSONException {
        if (this.string == null) {
            int c;
            int length = 0;
            final char chars[] = new char[this.length];
            for (int at = 0; at < this.length; at += characterSize(c)) {
                c = this.characterAt(at);
                if (c < 0x10000) {
                    chars[length] = (char) c;
                    length += 1;
                } else {
                    chars[length] = (char) (0xD800 | ((c - 0x10000) >>> 10));
                    length += 1;
                    chars[length] = (char) (0xDC00 | (c & 0x03FF));
                    length += 1;
                }
            }
            this.string = new String(chars, 0, length);
        }
        return this.string;
    }
}
