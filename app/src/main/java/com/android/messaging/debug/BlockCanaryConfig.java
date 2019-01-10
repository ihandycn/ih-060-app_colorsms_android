package com.android.messaging.debug;

import android.content.Context;
import android.text.TextUtils;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.internal.BlockInfo;

public class BlockCanaryConfig extends BlockCanaryContext {

    private static final long UPLOAD_BLOCK_TIME_THRESHOLD = 3000;

    // This is default block threshold, you can set it by phone's performance
    @Override
    public int provideBlockThreshold() {
        return 200;
    }

    // If set true, notification will be shown, else only write log file
    @Override
    public boolean displayNotification() {
        return false;
    }

    // Path to save log file
    @Override
    public String providePath() {
        return super.providePath();
    }

    @Override
    public void onBlock(Context context, BlockInfo blockInfo) {
        super.onBlock(context, blockInfo);
        if (blockInfo.timeCost > UPLOAD_BLOCK_TIME_THRESHOLD) {
            sendBlockToSlack(blockInfo);
        }
    }

    private void sendBlockToSlack(BlockInfo blockInfo) {
        String description = getBlockDescription(blockInfo);
        if (description != null) {
            SlackUtils.sendBlock(getBlockDescription(blockInfo));
        }
    }

    /**
     * @return {@code null} when the block can be ignored.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    private String getBlockDescription(BlockInfo blockInfo) {
        String fullDescription = blockInfo.toString();
        try {
            int stackStart = fullDescription.indexOf("stack =");
            String stackSection = fullDescription.substring(stackStart);
            String[] stackSectionLines = stackSection.split("\\r?\\n");
            for (String line : stackSectionLines) {
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                if (line.startsWith("android.os.MessageQueue.nativePollOnce")) {
                    return null;
                }
                try {
                    String fullMethodName = line.substring(0, line.indexOf("("));
                    String[] packageSegments = fullMethodName.split("\\.");
                    int segmentCount = packageSegments.length;
                    String description = "Block at " + packageSegments[segmentCount - 2] + "."
                                                     + packageSegments[segmentCount - 1] + "()\n"
                                                     + fullDescription;
                    return description;
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return fullDescription;
        }
        return fullDescription;
    }
}
