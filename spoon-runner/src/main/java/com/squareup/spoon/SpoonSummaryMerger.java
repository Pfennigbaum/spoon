package com.squareup.spoon;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

/**
 * Created by Frederik Vinnen on 23.09.2016.
 */
public class SpoonSummaryMerger {

    public static SpoonSummary mergeSpoonSummaryWithExistingSummaryFromFolder(SpoonSummary summary, File outputFolder){
        if (outputFolder.isDirectory()) {
            File existingSummaryFile = new File(outputFolder,"result.json");
            if (existingSummaryFile.exists()) {
                try {
                    SpoonSummary existingSummary = SpoonUtils.GSON.fromJson(new FileReader(existingSummaryFile),SpoonSummary.class);
                    if (existingSummary != null) {
                        Map<String,DeviceResult> resultMap = existingSummary.getResults();
                        for (String key : summary.getResults().keySet()) { //resolve duplicate serials
                            if (resultMap.containsKey(key)) {
                                int i = 1;
                                String newKey = key+"-"+i;
                                while (resultMap.containsKey(newKey)){
                                    i++;
                                    newKey = key+"-"+i;
                                }
                                resultMap.put(newKey,summary.getResults().get(key));
                            }else{
                                resultMap.put(key,summary.getResults().get(key));
                            }
                        }
                        IRemoteAndroidTestRunner.TestSize testSize = existingSummary.getTestSize();
                        if(!summary.getTestSize().equals(existingSummary.getTestSize())){
                            testSize = null;
                            SpoonLogger.logInfo("TestSize mismatch between current summary[%s] and found summary[%s], will use no TestSize",summary.getTestSize(),existingSummary.getTestSize());
                        }
                        if(!summary.getTitle().equals(existingSummary.getTitle())){
                            SpoonLogger.logInfo("Title mismatch between current summary title[%s] and existing summary title[%s], will use existing Title[%s]",summary.getTitle(),existingSummary.getTitle(),existingSummary.getTitle());
                        }
                        SpoonSummary mergedSpoonSummary = new SpoonSummary(existingSummary.getTitle(),testSize,existingSummary.getStarted(),existingSummary.getDuration()+summary.getDuration(),resultMap);
                        return mergedSpoonSummary;
                    }else{
                        SpoonLogger.logError("merge failed - Could not parse existing result.json file!");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                SpoonLogger.logError("merge failed - Could not find existing result.json file!");
            }
        }else{
            SpoonLogger.logError("merge failed - output directory is empty, could not find existing summary to merge into");
        }
        return summary;
    }
}
