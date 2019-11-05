// Copyright (C) 2019 Karakun AG
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.adoptopenjdk.icedteaweb.resources.downloader;

import net.adoptopenjdk.icedteaweb.jnlp.version.VersionId;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allows to unpack an input stream.
 */
interface StreamUnpacker {

    static StreamUnpacker getCompressionUnpacker(final DownloadDetails downloadDetails) {
        final URL downloadFrom = downloadDetails.downloadFrom;
        final String contentEncoding = downloadDetails.contentEncoding;
        final boolean packgz = "pack200-gzip".equals(contentEncoding) || downloadFrom.getPath().endsWith(".pack.gz");
        final boolean gzip = "gzip".equals(contentEncoding);

        // It's important to check packgz first. If a stream is both
        // pack200 and gz encoded, then con.getContentEncoding() could
        // return ".gz", so if we check gzip first, we would end up
        // treating a pack200 file as a jar file.
        if (packgz) {
            return new PackGzipUnpacker();
        } else if (gzip) {
            return new GzipUnpacker();
        }

        return new NotUnpacker();
    }

    static StreamUnpacker getContentUnpacker(DownloadDetails downloadDetails, URL resourceHref) {
        final StreamUnpacker contentUnpacker;
        if (downloadDetails.contentType.startsWith(BaseResourceDownloader.JAR_DIFF_MIME_TYPE)) {
            final Map<String, String> querryParams = Optional.ofNullable(downloadDetails.downloadFrom.getQuery())
                    .map(query -> Stream.of(query.split(Pattern.quote("&"))))
                    .map(stream -> stream.collect(Collectors.toMap(e -> e.split("=")[0], e -> e.split("=")[1])))
                    .orElseGet(Collections::emptyMap);

            final VersionId currentVersionId = Optional.ofNullable(querryParams.get("current-version-id"))
                    .map(VersionId::fromString)
                    .orElseThrow(() -> new IllegalArgumentException("Mime-Type " + BaseResourceDownloader.JAR_DIFF_MIME_TYPE + " for non incremental request to " + downloadDetails.downloadFrom));

            final File cacheFile = Cache.getCacheFile(resourceHref, currentVersionId);
            contentUnpacker = new JarDiffUnpacker(cacheFile);
        } else {
            contentUnpacker = new NotUnpacker();
        }
        return contentUnpacker;
    }

    /**
     * Unpacks the content of the input stream.
     * Provides a new input stream with the unpacked content.
     *
     * @param input a compressed input stream
     * @return an unpacked input stream
     * @throws IOException if anything goes wrong
     */
    InputStream unpack(InputStream input) throws IOException;
}
