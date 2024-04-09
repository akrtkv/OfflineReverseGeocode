/*
The MIT License (MIT)
[OSI Approved License]
The MIT License (MIT)

Copyright (c) 2014 Daniel Glasson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package geocode;

import geocode.kdtree.KDTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Daniel Glasson on 18/05/2014.
 * Uses KD-trees to quickly find the nearest point
 * <p>
 * ReverseGeoCode reverseGeoCode = new ReverseGeoCode(new FileInputStream("c:\\AU.txt"), true);
 * System.out.println("Nearest to -23.456, 123.456 is " + geocode.nearestPlace(-23.456, 123.456));
 */
public class ReverseGeoCode {

    KDTree<GeoName> kdTree;

    ArrayList<GeoName> arPlaceNames;

    // Get placenames from http://download.geonames.org/export/dump/

    /**
     * Parse the raw text geonames file.
     *
     * @param placenames the text file downloaded from http://download.geonames.org/export/dump/; can not be null.
     * @param majorOnly  only include major cities in KD-tree.
     * @throws IOException          if there is a problem reading the stream.
     * @throws NullPointerException if zippedPlacenames is {@code null}.
     */
    public ReverseGeoCode(InputStream placenames, boolean majorOnly) throws IOException {
        createKdTree(placenames, majorOnly);
    }

    private void createKdTree(InputStream placenames, boolean majorOnly) throws IOException {
        arPlaceNames = new ArrayList<GeoName>();
        // Read the geonames file in the directory
        try (BufferedReader in = new BufferedReader(new InputStreamReader(placenames))) {
            String str;
            while ((str = in.readLine()) != null) {
                GeoName newPlace = new GeoName(str);
                if (!majorOnly || newPlace.majorPlace) {
                    arPlaceNames.add(newPlace);
                }
            }
        }
        kdTree = new KDTree<GeoName>(arPlaceNames);
    }

    public GeoName nearestPlace(double latitude, double longitude) {
        return kdTree.findNearest(new GeoName(latitude, longitude));
    }

    public GeoName getByCityName(String cityName) {
        GeoName geoName = null;

        for (GeoName g : this.arPlaceNames) {
            if (g.getName().equalsIgnoreCase(cityName) || g.getAsciiName().equalsIgnoreCase(cityName) || containsIgnoreCase(g.getAlternateNames(), cityName)) {
                geoName = g;
            }
        }

        return geoName;
    }

    private boolean containsIgnoreCase(CharSequence str, CharSequence searchStr) {
        if (str != null && searchStr != null) {
            int len = searchStr.length();
            int max = str.length() - len;

            for (int i = 0; i <= max; ++i) {
                if (regionMatches(str, true, i, searchStr, 0, len)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean regionMatches(CharSequence cs, boolean ignoreCase, int thisStart, CharSequence substring, int start, int length) {
        return cs instanceof String && substring instanceof String ? ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length) : cs.toString().regionMatches(ignoreCase, thisStart, substring.toString(), start, length);
    }
}
