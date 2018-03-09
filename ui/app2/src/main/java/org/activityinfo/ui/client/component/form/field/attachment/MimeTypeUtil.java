/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
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
package org.activityinfo.ui.client.component.form.field.attachment;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author yuriyz on 8/8/14.
 */
public class MimeTypeUtil {

    private static Map<String, String> fileExtensionToMimeTypeRegistry = Maps.newHashMap();

    static {
        // Image
        fileExtensionToMimeTypeRegistry.put("gif", "image/gif");
        fileExtensionToMimeTypeRegistry.put("jpg", "image/jpeg");
        fileExtensionToMimeTypeRegistry.put("jpeg", "image/jpeg");
        fileExtensionToMimeTypeRegistry.put("jpe", "image/jpeg");
        fileExtensionToMimeTypeRegistry.put("jfif", "image/pipeg");
        fileExtensionToMimeTypeRegistry.put("pjpeg", "image/pjpeg");
        fileExtensionToMimeTypeRegistry.put("png", "image/png");
        fileExtensionToMimeTypeRegistry.put("svg", "image/svg+xml");
        fileExtensionToMimeTypeRegistry.put("tiff", "image/tiff");
        fileExtensionToMimeTypeRegistry.put("bmp", "image/bmp");

        // Text
        fileExtensionToMimeTypeRegistry.put(".txt", "text/plain");
        fileExtensionToMimeTypeRegistry.put("txt", "text/plain");
        fileExtensionToMimeTypeRegistry.put("323", "text/h323");
        
        fileExtensionToMimeTypeRegistry.put("*", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("acx", "application/internet-property-stream");
        fileExtensionToMimeTypeRegistry.put("ai", "application/postscript");
        fileExtensionToMimeTypeRegistry.put("aif", "audio/x-aiff");
        fileExtensionToMimeTypeRegistry.put("aifc", "audio/x-aiff");
        fileExtensionToMimeTypeRegistry.put("aiff", "audio/x-aiff");
        fileExtensionToMimeTypeRegistry.put("asf", "video/x-ms-asf");
        fileExtensionToMimeTypeRegistry.put("asr", "video/x-ms-asf");
        fileExtensionToMimeTypeRegistry.put("asx", "video/x-ms-asf");
        fileExtensionToMimeTypeRegistry.put("au", "audio/basic");
        fileExtensionToMimeTypeRegistry.put("avi", "video/x-msvideo");
        fileExtensionToMimeTypeRegistry.put("axs", "application/olescript");
        fileExtensionToMimeTypeRegistry.put("bas", "text/plain");
        fileExtensionToMimeTypeRegistry.put("bcpio", "application/x-bcpio");
        fileExtensionToMimeTypeRegistry.put("bin", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("c", "text/plain");
        fileExtensionToMimeTypeRegistry.put("cat", "application/vnd.ms-pkiseccat");
        fileExtensionToMimeTypeRegistry.put("cdf", "application/x-cdf");
        fileExtensionToMimeTypeRegistry.put("cer", "application/x-x509-ca-cert");
        fileExtensionToMimeTypeRegistry.put("class", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("cer", "application/x-x509-ca-cert");
        fileExtensionToMimeTypeRegistry.put("class", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("clp", "application/x-msclip");
        fileExtensionToMimeTypeRegistry.put("cmx", "image/x-cmx");
        fileExtensionToMimeTypeRegistry.put("cod", "image/cis-cod");
        fileExtensionToMimeTypeRegistry.put("cpio", "application/x-cpio");
        fileExtensionToMimeTypeRegistry.put("crd", "application/x-mscardfile");
        fileExtensionToMimeTypeRegistry.put("crl", "application/pkix-crl");
        fileExtensionToMimeTypeRegistry.put("crt", "application/x-x509-ca-cert");
        fileExtensionToMimeTypeRegistry.put("csh", "application/x-csh");
        fileExtensionToMimeTypeRegistry.put("css", "text/css");
        fileExtensionToMimeTypeRegistry.put("dcr", "application/x-director");
        fileExtensionToMimeTypeRegistry.put("der", "application/x-x509-ca-cert");
        fileExtensionToMimeTypeRegistry.put("dir", "application/x-director");
        fileExtensionToMimeTypeRegistry.put("dll", "application/x-msdownload");
        fileExtensionToMimeTypeRegistry.put("dms", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("doc", "application/msword");
        fileExtensionToMimeTypeRegistry.put("dot", "application/msword");
        fileExtensionToMimeTypeRegistry.put("dvi", "application/x-dvi");
        fileExtensionToMimeTypeRegistry.put("dxr", "application/x-director");
        fileExtensionToMimeTypeRegistry.put("eps", "application/postscript");
        fileExtensionToMimeTypeRegistry.put("etx", "text/x-setext");
        fileExtensionToMimeTypeRegistry.put("evy", "application/envoy");
        fileExtensionToMimeTypeRegistry.put("exe", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("fif", "application/fractals");
        fileExtensionToMimeTypeRegistry.put("flr", "x-world/x-vrml");
        fileExtensionToMimeTypeRegistry.put("gif", "image/gif");
        fileExtensionToMimeTypeRegistry.put("gtar", "application/x-gtar");
        fileExtensionToMimeTypeRegistry.put("gz", "application/x-gzip");
        fileExtensionToMimeTypeRegistry.put("h", "text/plain");
        fileExtensionToMimeTypeRegistry.put("hdf", "application/x-hdf");
        fileExtensionToMimeTypeRegistry.put("hlp", "application/winhlp");
        fileExtensionToMimeTypeRegistry.put("hqx", "application/mac-binhex40");
        fileExtensionToMimeTypeRegistry.put("hta", "application/hta");
        fileExtensionToMimeTypeRegistry.put("htc", "text/x-component");
        fileExtensionToMimeTypeRegistry.put("htm", "text/html");
        fileExtensionToMimeTypeRegistry.put("html", "text/html");
        fileExtensionToMimeTypeRegistry.put("htt", "text/webviewhtml");
        fileExtensionToMimeTypeRegistry.put("ico", "image/x-icon");
        fileExtensionToMimeTypeRegistry.put("ief", "image/ief");
        fileExtensionToMimeTypeRegistry.put("iii", "application/x-iphone");
        fileExtensionToMimeTypeRegistry.put("ins", "application/x-internet-signup");
        fileExtensionToMimeTypeRegistry.put("isp", "application/x-internet-signup");
        fileExtensionToMimeTypeRegistry.put("js", "application/x-javascript");
        fileExtensionToMimeTypeRegistry.put("latex", "application/x-latex");
        fileExtensionToMimeTypeRegistry.put("lha", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("lsf", "video/x-la-asf");
        fileExtensionToMimeTypeRegistry.put("lsx", "video/x-la-asf");
        fileExtensionToMimeTypeRegistry.put("lzh", "application/octet-stream");
        fileExtensionToMimeTypeRegistry.put("m13", "application/x-msmediaview");
        fileExtensionToMimeTypeRegistry.put("m14", "application/x-msmediaview");
        fileExtensionToMimeTypeRegistry.put("m3u", "audio/x-mpegurl");
        fileExtensionToMimeTypeRegistry.put("man", "application/x-troff-man");
        fileExtensionToMimeTypeRegistry.put("mdb", "application/x-msaccess");
        fileExtensionToMimeTypeRegistry.put("me", "application/x-troff-me");
        fileExtensionToMimeTypeRegistry.put("mht", "message/rfc822");
        fileExtensionToMimeTypeRegistry.put("mhtml", "message/rfc822");
        fileExtensionToMimeTypeRegistry.put("mid", "audio/mid");
        fileExtensionToMimeTypeRegistry.put("mny", "application/x-msmoney");
        fileExtensionToMimeTypeRegistry.put("mov", "video/quicktime");
        fileExtensionToMimeTypeRegistry.put("movie", "video/x-sgi-movie");
        fileExtensionToMimeTypeRegistry.put("mp2", "video/mpeg");
        fileExtensionToMimeTypeRegistry.put("mp3", "audio/mpeg");
        fileExtensionToMimeTypeRegistry.put("mpa", "video/mpeg");
        fileExtensionToMimeTypeRegistry.put("mpe", "video/mpeg");
        fileExtensionToMimeTypeRegistry.put("mpeg", "video/mpeg");
        fileExtensionToMimeTypeRegistry.put("mpg", "video/mpeg");
        fileExtensionToMimeTypeRegistry.put("mpp", "application/vnd.ms-project");
        fileExtensionToMimeTypeRegistry.put("mpv2", "video/mpeg");
        fileExtensionToMimeTypeRegistry.put("ms", "application/x-troff-ms");
        fileExtensionToMimeTypeRegistry.put("msg", "application/vnd.ms-outlook");
        fileExtensionToMimeTypeRegistry.put("mvb", "application/x-msmediaview");
        fileExtensionToMimeTypeRegistry.put("nc", "application/x-netcdf");
        fileExtensionToMimeTypeRegistry.put("nws", "message/rfc822");
        fileExtensionToMimeTypeRegistry.put("oda", "application/oda");
        fileExtensionToMimeTypeRegistry.put("p10", "application/pkcs10");
        fileExtensionToMimeTypeRegistry.put("p12", "application/x-pkcs12");
        fileExtensionToMimeTypeRegistry.put("p7b", "application/x-pkcs7-certificates");
        fileExtensionToMimeTypeRegistry.put("p7c", "application/x-pkcs7-mime");
        fileExtensionToMimeTypeRegistry.put("p7m", "application/x-pkcs7-mime");
        fileExtensionToMimeTypeRegistry.put("p7r", "application/x-pkcs7-certreqresp");
        fileExtensionToMimeTypeRegistry.put("p7s", "application/x-pkcs7-signature");
        fileExtensionToMimeTypeRegistry.put("pbm", "image/x-portable-bitmap");
        fileExtensionToMimeTypeRegistry.put("pdf", "application/pdf");
        fileExtensionToMimeTypeRegistry.put("pfx", "application/x-pkcs12");
        fileExtensionToMimeTypeRegistry.put("pgm", "image/x-portable-graymap");
        fileExtensionToMimeTypeRegistry.put("pko", "application/ynd.ms-pkipko");
        fileExtensionToMimeTypeRegistry.put("pma", "application/x-perfmon");
        fileExtensionToMimeTypeRegistry.put("pmc", "application/x-perfmon");
        fileExtensionToMimeTypeRegistry.put("pml", "application/x-perfmon");
        fileExtensionToMimeTypeRegistry.put("pmr", "application/x-perfmon");
        fileExtensionToMimeTypeRegistry.put("pmw", "application/x-perfmon");
        fileExtensionToMimeTypeRegistry.put("pnm", "image/x-portable-anymap");
        fileExtensionToMimeTypeRegistry.put("pot", "application/vnd.ms-powerpoint");
        fileExtensionToMimeTypeRegistry.put("ppm", "image/x-portable-pixmap");
        fileExtensionToMimeTypeRegistry.put("pps", "application/vnd.ms-powerpoint");
        fileExtensionToMimeTypeRegistry.put("ppt", "application/vnd.ms-powerpoint");
        fileExtensionToMimeTypeRegistry.put("prf", "application/pics-rules");
        fileExtensionToMimeTypeRegistry.put("ps", "application/postscript");
        fileExtensionToMimeTypeRegistry.put("pub", "application/x-mspublisher");
        fileExtensionToMimeTypeRegistry.put("qt", "video/quicktime");
        fileExtensionToMimeTypeRegistry.put("ra", "audio/x-pn-realaudio");
        fileExtensionToMimeTypeRegistry.put("ram", "audio/x-pn-realaudio");
        fileExtensionToMimeTypeRegistry.put("ras", "image/x-cmu-raster");
        fileExtensionToMimeTypeRegistry.put("rgb", "image/x-rgb");
        fileExtensionToMimeTypeRegistry.put("rmi", "audio/mid");
        fileExtensionToMimeTypeRegistry.put("roff", "application/x-troff");
        fileExtensionToMimeTypeRegistry.put("rtf", "application/rtf");
        fileExtensionToMimeTypeRegistry.put("rtx", "text/richtext");
        fileExtensionToMimeTypeRegistry.put("scd", "application/x-msschedule");
        fileExtensionToMimeTypeRegistry.put("sct", "text/scriptlet");
        fileExtensionToMimeTypeRegistry.put("setpay", "application/set-payment-initiation");
        fileExtensionToMimeTypeRegistry.put("setreg", "application/set-registration-initiation");
        fileExtensionToMimeTypeRegistry.put("sh", "application/x-sh");
        fileExtensionToMimeTypeRegistry.put("shar", "application/x-shar");
        fileExtensionToMimeTypeRegistry.put("sit", "application/x-stuffit");
        fileExtensionToMimeTypeRegistry.put("snd", "audio/basic");
        fileExtensionToMimeTypeRegistry.put("spc", "application/x-pkcs7-certificates");
        fileExtensionToMimeTypeRegistry.put("spl", "application/futuresplash");
        fileExtensionToMimeTypeRegistry.put("src", "application/x-wais-source");
        fileExtensionToMimeTypeRegistry.put("sst", "application/vnd.ms-pkicertstore");
        fileExtensionToMimeTypeRegistry.put("stl", "application/vnd.ms-pkistl");
        fileExtensionToMimeTypeRegistry.put("stm", "text/html");
        fileExtensionToMimeTypeRegistry.put("sv4cpio", "application/x-sv4cpio");
        fileExtensionToMimeTypeRegistry.put("sv4crc", "application/x-sv4crc");
        fileExtensionToMimeTypeRegistry.put("svg", "image/svg+xml");
        fileExtensionToMimeTypeRegistry.put("swf", "application/x-shockwave-flash");
        fileExtensionToMimeTypeRegistry.put("t", "application/x-troff");
        fileExtensionToMimeTypeRegistry.put("tar", "application/x-tar");
        fileExtensionToMimeTypeRegistry.put("tcl", "application/x-tcl");
        fileExtensionToMimeTypeRegistry.put("tex", "application/x-tex");
        fileExtensionToMimeTypeRegistry.put("texi", "application/x-texinfo");
        fileExtensionToMimeTypeRegistry.put("texinfo", "application/x-texinfo");
        fileExtensionToMimeTypeRegistry.put("tgz", "application/x-compressed");
        fileExtensionToMimeTypeRegistry.put("tif", "image/tiff");
        fileExtensionToMimeTypeRegistry.put("tiff", "image/tiff");
        fileExtensionToMimeTypeRegistry.put("tr", "application/x-troff");
        fileExtensionToMimeTypeRegistry.put("trm", "application/x-msterminal");
        fileExtensionToMimeTypeRegistry.put("tsv", "text/tab-separated-values");
        fileExtensionToMimeTypeRegistry.put("uls", "text/iuls");
        fileExtensionToMimeTypeRegistry.put("ustar", "application/x-ustar");
        fileExtensionToMimeTypeRegistry.put("vcf", "text/x-vcard");
        fileExtensionToMimeTypeRegistry.put("vrml", "x-world/x-vrml");
        fileExtensionToMimeTypeRegistry.put("wav", "audio/x-wav");
        fileExtensionToMimeTypeRegistry.put("wcm", "application/vnd.ms-works");
        fileExtensionToMimeTypeRegistry.put("wdb", "application/vnd.ms-works");
        fileExtensionToMimeTypeRegistry.put("wks", "application/vnd.ms-works");
        fileExtensionToMimeTypeRegistry.put("wmf", "application/x-msmetafile");
        fileExtensionToMimeTypeRegistry.put("wps", "application/vnd.ms-works");
        fileExtensionToMimeTypeRegistry.put("wri", "application/x-mswrite");
        fileExtensionToMimeTypeRegistry.put("wrl", "x-world/x-vrml");
        fileExtensionToMimeTypeRegistry.put("wrz", "x-world/x-vrml");
        fileExtensionToMimeTypeRegistry.put("xaf", "x-world/x-vrml");
        fileExtensionToMimeTypeRegistry.put("xbm", "image/x-xbitmap");
        fileExtensionToMimeTypeRegistry.put("xla", "application/vnd.ms-excel");
        fileExtensionToMimeTypeRegistry.put("xlc", "application/vnd.ms-excel");
        fileExtensionToMimeTypeRegistry.put("xlm", "application/vnd.ms-excel");
        fileExtensionToMimeTypeRegistry.put("xls", "application/vnd.ms-excel");
        fileExtensionToMimeTypeRegistry.put("xlt", "application/vnd.ms-excel");
        fileExtensionToMimeTypeRegistry.put("xlw", "application/vnd.ms-excel");
        fileExtensionToMimeTypeRegistry.put("xof", "x-world/x-vrml");
        fileExtensionToMimeTypeRegistry.put("xpm", "image/x-xpixmap");
        fileExtensionToMimeTypeRegistry.put("xwd", "image/x-xwindowdump");
        fileExtensionToMimeTypeRegistry.put("z", "application/x-compress");
        fileExtensionToMimeTypeRegistry.put("zip", "application/zip");
    }

    public static String mimeTypeFromExtension(String fileExtension) {
        if (!fileExtensionToMimeTypeRegistry.containsKey(fileExtension)) {
            throw new UnsupportedOperationException("Unknown file extension: " + fileExtension);
        }
        return fileExtensionToMimeTypeRegistry.get(fileExtension);
    }

    public static String mimeTypeFromFileExtension(String fileExtension) {
        return fileExtensionToMimeTypeRegistry.get(fileExtension);
    }

    public static String fileExtension(String filename) {
        int i = filename.lastIndexOf(".");
        if (i != -1) {
            return filename.substring(i + 1);
        }
        return filename;
    }

    public static String mimeTypeFromFileName(String fileName) {
        return mimeTypeFromFileExtension(fileExtension(fileName));
    }

    public static String mimeTypeFromFileName(String fileName, String defaultValue) {
        String mimeType = mimeTypeFromFileExtension(fileExtension(fileName));
        if (Strings.isNullOrEmpty(mimeType)) {
            mimeType = defaultValue;
        }
        return mimeType;
    }
}
