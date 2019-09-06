package com.android.messaging.ui.mediapicker.sendcontact;

import android.net.Uri;
import android.text.TextUtils;

import com.android.messaging.datamodel.MediaScratchFileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ContactFileCreator {
    public static Uri create(String firstName, String surname, String telephone) {
        Uri uri = MediaScratchFileProvider.buildMediaScratchSpaceUri("vcf");
        File vcfFile = MediaScratchFileProvider.getFileFromUri(uri);
        FileWriter fw;
        try {
            fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:2.1\r\n");
            if (!TextUtils.isEmpty(surname)) {
                fw.write("N:" + surname + ";" + firstName + "\r\n");
                fw.write("FN:" + firstName + " " + surname + "\r\n");
            } else {
                fw.write("N:" + firstName + "\r\n");
                fw.write("FN:" + firstName + "\r\n");
            }
            fw.write("TEL;CELL:" + telephone + "\r\n");
            fw.write("END:VCARD\r\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return uri;
    }
}
