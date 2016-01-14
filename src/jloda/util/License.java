/**
 * License.java 
 * Copyright (C) 2016 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jloda.util;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * a license data object
 * Daniel Huson, 4.2013
 */
public class License {
    public enum Item {Program, User, Email, Organization, Address, City, Country, IPAddress, LicenseType, ExpireDate, Signature}

    public enum Type {
        Academic, SingleUser, Site, Company, Temporary, NonProfitResearchLab;

        public String getInfo() {
            switch (this) {
                default:
                case Academic:
                    return "academic research, publication and teaching only.";
                case SingleUser:
                    return "only to be used by person specified under 'User'.";
                case Site:
                    return "only to be used at the site specified under 'Address' and 'City'.";
                case Company:
                    return "only to be used within the company specified under 'Organization'.";
                case Temporary:
                    return "only to be used for evaluation or teaching purposes.";
                case NonProfitResearchLab:
                    return "non-profit research, publication and teaching only.";
            }
        }
    }

    private final Map<Item, String> item2value = new HashMap<>();

    /**
     * set a list of name to value pairs
     *
     * @param pairs
     */
    public void setPairs(java.util.Collection<Pair<String, String>> pairs) {
        for (Pair<String, String> name2value : pairs) {
            final Item item = Item.valueOf(name2value.get1());
            if (item != null) {
                String value = name2value.get2().trim();
                if (item == Item.ExpireDate) {
                    if (value.length() == 0 || value.equals("0")) {
                        if (item2value.containsKey(item))
                            item2value.remove(item);
                    } else {
                        if (value.equals("1")) // one year
                        {
                            value = (new Date((long) (System.currentTimeMillis() + 3.419e+10))).toString();
                        } else if (value.equals("2")) // two years
                        {
                            value = (new Date((long) (System.currentTimeMillis() + 2 * 3.419e+10))).toString();
                        }
                        item2value.put(item, value);
                    }
                } else
                    item2value.put(item, value);
            }
        }
    }


    /**
     * string representation
     *
     * @return as string
     */
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        for (Item item : Item.values()) {
            if (item2value.containsKey(item)) {
                buf.append(item.toString()).append(": ").append(item2value.get(item)).append("\n");
            }
        }
        return buf.toString();
    }

    /**
     * returns the size
     *
     * @return size
     */
    public int size() {
        return item2value.size();
    }

    /**
     * erase the license info
     */
    public void clear() {
        item2value.clear();
    }

    /**
     * string representation
     *
     * @return as string
     */
    public String toStringWithoutSignature() {
        StringBuilder buf = new StringBuilder();
        for (Item item : Item.values()) {
            if (item != Item.Signature && item2value.containsKey(item)) {
                String value = item2value.get(item);
                if (item == Item.LicenseType && !value.contains(" -"))
                    value = value + " - " + Type.valueOf(value).getInfo();
                buf.append(item.toString()).append(": ").append(value.trim()).append("\n");
            }
        }
        return buf.toString();
    }

    /**
     * put the value for an item
     *
     * @param item
     * @param value
     */
    public void put(Item item, String value) {
        item2value.put(item, value);
    }

    /**
     * get the value for an item
     *
     * @param item
     * @return value
     */
    public String get(Item item) {
        return item2value.get(item);
    }

    /**
     * gets data as bytes  without the signature (for verifying)
     *
     * @return as bytes
     */
    public byte[] getBytes() {
        try {
            // String str= toStringWithoutSignature();
            // System.err.println("String:\n"+str);
            //  System.err.println("Length: "+str.length());
            //  System.err.println("Hash: "+str.hashCode());

            // replace all non-ascii characters by double ?? before computing score
            // use ?? because this is what non-ascii characters result in on web page
            return toStringWithoutSignature().replaceAll("\\P{InBasic_Latin}", "??").getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return toStringWithoutSignature().getBytes();
        }
    }

    /**
     * save to file
     *
     * @param fileName
     * @throws IOException
     */
    public void writeToFile(String fileName) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
            w.write(toString());
        }
    }

    /**
     * load from a file
     *
     * @param file
     * @throws IOException
     */
    public void loadFromFile(File file) {
        item2value.clear();

        ICloseableIterator<String> it = null;
        try {
            if ((new RTFFileFilter()).accept(file.getParentFile(), file.getName())) {
                System.err.println("This file appears to be in RTF format, not TXT format, will try to parse. If unsuccessful, please save as a TXT file and let me try again.");
                final String[] lines = RTFFileFilter.getStrippedLines(file);
                it = new ICloseableIterator<String>() {
                    private int i = 0;

                    public void close() throws IOException {
                    }

                    public long getMaximumProgress() {
                        return lines.length;
                    }

                    public long getProgress() {
                        return i;
                    }

                    public boolean hasNext() {
                        return i < lines.length;
                    }

                    public String next() {
                        return lines[i++];
                    }

                    public void remove() {

                    }
                };
            } else
            it = new FileInputIterator(file.getPath());

            boolean inLicense = false;
            boolean waitForSignature = false; // often the signature token and the actual singature or on different lines, try to fix this
            while (it.hasNext()) {
                final String aLine = it.next();

                if (aLine.length() > 0 && !aLine.startsWith("#")) {
                    if (!inLicense) {
                        if (aLine.equals("License certificate:") || aLine.equals("Registration details:"))  // the latter for legacy
                            inLicense = true;
                    } else {
                        final int pos = aLine.indexOf(':');
                        if (pos != -1) {
                            final String key = aLine.substring(0, pos).trim();
                            final String value = pos + 1 < aLine.length() ? aLine.substring(pos + 1, aLine.length()).trim() : null;
                            final Item item = Item.valueOf(key);
                            item2value.put(item, value);
                            if (item.equals(Item.Signature)) {
                                if (value == null && it.hasNext()) // signature appears to be on next line
                                {
                                    item2value.put(item, it.next().trim());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (it != null)
                    it.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * verify that signature is valid
     *
     * @param signer
     * @return true, if signature is valid
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    public boolean verifySignature(Signer signer) {
        try {
            return signer.verifySignedData(getBytes(), Signer.signatureHexStringToBytes(get(Item.Signature)));
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * gets licensed-to header string
     *
     * @return string
     */
    public String getLicensedTo() {
        if (item2value.size() > 0) {
            final String value = item2value.get(Item.LicenseType);
            if (value != null) {
                String typeName = Basic.getFirstWord(value);
                switch (Type.valueOf(typeName)) {
                    case Academic: {
                        StringWriter w = new StringWriter();
                        w.write("Licensed to user: " + item2value.get(Item.User) + ", ");
                        w.write("Academic institution: " + item2value.get(Item.Organization) + ", ");
                        w.write("Email: " + item2value.get(Item.Email));
                        return w.toString();
                    }
                    case SingleUser: {
                        StringWriter w = new StringWriter();
                        w.write("Licensed to user: " + item2value.get(Item.User) + ", ");
                        w.write("Organization: " + item2value.get(Item.Organization) + ", ");
                        w.write("Email: " + item2value.get(Item.Email));
                        return w.toString();
                    }
                    case Site: {
                        StringWriter w = new StringWriter();
                        w.write("Licensed to Site: " + item2value.get(Item.City) + ", ");
                        w.write("Organization: " + item2value.get(Item.Organization) + ", ");
                        w.write("Email: " + item2value.get(Item.Email));
                        return w.toString();
                    }
                    case Company: {
                        StringWriter w = new StringWriter();
                        w.write("Licensed to company: " + item2value.get(Item.Organization) + ", ");
                        w.write("Contact: " + item2value.get(Item.User) + ", ");
                        w.write("Email: " + item2value.get(Item.Email));
                        return w.toString();
                    }
                    case Temporary: {
                        StringWriter w = new StringWriter();
                        w.write("Temporary license ");
                        w.write("User: " + item2value.get(Item.User) + ", ");
                        w.write("Organization: " + item2value.get(Item.Organization));
                        return w.toString();
                    }
                    case NonProfitResearchLab: {
                        StringWriter w = new StringWriter();
                        w.write("Non-profit license ");
                        w.write("User: " + item2value.get(Item.User) + ", ");
                        w.write("Organization: " + item2value.get(Item.Organization));
                        return w.toString();
                    }
                }
            }
        }
        return "No valid license - for evaluation only";
    }
}
