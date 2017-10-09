/*
 * Copyright Anil Muppalla.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.anil.junix;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

public class LS {

    public static final Logger logger = LoggerFactory.getLogger(LS.class);

    @Parameter(names = {"-l"}, description = "Long list format")
    private Boolean l = false;

    public static void main(String... argv) {
        LS ls = new LS();
        JCommander.newBuilder()
                .addObject(ls)
                .build()
                .parse(argv);

        ls.run();

    }

    private String permissions(final Set<PosixFilePermission> permissionSet) {

        Map<String, String> filePermsMap = new HashMap<>();
        filePermsMap.put("OWNER_READ", "r");
        filePermsMap.put("GROUP_READ", "r");
        filePermsMap.put("OTHERS_READ", "r");
        filePermsMap.put("OWNER_WRITE", "w");
        filePermsMap.put("GROUP_WRITE", "w");
        filePermsMap.put("OTHERS_WRITE", "w");
        filePermsMap.put("OWNER_EXECUTE", "x");
        filePermsMap.put("GROUP_EXECUTE", "x");
        filePermsMap.put("OTHERS_EXECUTE", "x");

        List<String> permOrder = new ArrayList<>(Arrays.asList("OWNER_READ", "OWNER_WRITE", "OWNER_EXECUTE",
                "GROUP_READ", "GROUP_WRITE", "GROUP_EXECUTE",
                "OTHERS_READ", "OTHERS_WRITE", "OTHERS_EXECUTE"));

        StringBuilder sb = new StringBuilder();

        List<String> filePerms = new ArrayList<>();

        for (PosixFilePermission posixFilePermission : permissionSet) {
            filePerms.add(posixFilePermission.name());
        }

        for (String s : permOrder) {
            String x = filePerms.contains(s) ? filePermsMap.get(s) : "-";
            sb.append(x);
        }

        return sb.toString();
    }

    private String longlist(final File file) {
        final List ll = new ArrayList<String>();

        try {
            Set perm = Files.getPosixFilePermissions(file.toPath());
            ll.add(permissions(perm));

        } catch (IOException io) {
            logger.error("Failed to get file permissions", io);
        }

        final long lm = file.lastModified();
        final DateTime dateTime = new DateTime(lm);
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM d HH:mm");
        final String lastModified = dateTime.toLocalDateTime().toString(formatter);

        try {
            ll.add(Files.getOwner(file.toPath()).getName());
        } catch (IOException io) {
            logger.error("Failed get owner", io);
        }

        try {
            ll.add(String.valueOf(file.length()));
        } catch (SecurityException se) {
            logger.error("Failed to size of the file", se);
        }

        ll.add(lastModified);

        return String.join(" ", ll);
    }

    private void run() {

        File folder = new File(".");
        File[] files = new File[]{};

        try {
            files = folder.listFiles();
        } catch (NullPointerException npe) {
            System.out.println(npe);
        }

        for (File file : files) {
            if (!file.isHidden()) {
                if (l) {
                    final String ll = longlist(file);
                    System.out.format("%s %s\n", ll, file.getName());
                } else {
                    System.out.format("%s \n", file.getName());
                }
            }
        }

    }

}
