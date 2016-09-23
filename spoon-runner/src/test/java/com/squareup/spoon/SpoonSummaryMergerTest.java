package com.squareup.spoon;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.squareup.spoon.html.HtmlRenderer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SpoonSummaryMergerTest {

  @Rule
  public TemporaryFolder testFolder1 = new TemporaryFolder();

  @Test
  public void shouldMergeCoverageFiles() throws Exception {
    File spoonOutputDirectory = testFolder1.newFolder("output");
    // PASS: Test success.
    SpoonSummary summary;
    DeviceTest device = new DeviceTest("foo", "bar");
    DeviceTest device2 = new DeviceTest("bar", "foo");
    summary = new SpoonSummary.Builder() //
            .setTitle("test") //
            .setTestSize(IRemoteAndroidTestRunner.TestSize.LARGE)//
            .start() //
            .addResult("123", new DeviceResult.Builder() //
                    .startTests() //
                    .addTestResultBuilder(device, new DeviceTestResult.Builder() //
                            .startTest() //
                            .endTest()) //
                    .addTestResultBuilder(device2, new DeviceTestResult.Builder() //
                            .startTest() //
                            .endTest()) //
                    .build()) //
            .end() //
            .build(); //

    new HtmlRenderer(summary,SpoonUtils.GSON,spoonOutputDirectory).render();

    // PASS: Test success.
    SpoonSummary summary2;
    DeviceTest device3 = new DeviceTest("bar", "foo");
    summary2 = new SpoonSummary.Builder() //
            .setTitle("test2") //
            .setTestSize(IRemoteAndroidTestRunner.TestSize.SMALL)
            .start() //
            .addResult("456", new DeviceResult.Builder() //
                    .startTests() //
                    .addTestResultBuilder(device3, new DeviceTestResult.Builder() //
                            .startTest() //
                            .endTest()) //
                    .build()) //
            .end() //
            .build(); //

    SpoonSummary mergedSummary = SpoonSummaryMerger.mergeSpoonSummaryWithExistingSummaryFromFolder(summary2,spoonOutputDirectory);
    File spoonOutputDirectory2 = testFolder1.newFolder("output2");
    new HtmlRenderer(summary,SpoonUtils.GSON,spoonOutputDirectory2).render();

    assertTrue(mergedSummary.getTitle().equals(summary.getTitle()));
    assertNull(mergedSummary.getTestSize());
    assertTrue(mergedSummary.getDuration() == summary.getDuration() + summary2.getDuration());
    for (String key : summary.getResults().keySet()) {
      assertTrue(mergedSummary.getResults().containsKey(key));
    }
    for (String key : summary2.getResults().keySet()) {
      assertTrue(mergedSummary.getResults().containsKey(key));
    }
    assertTrue(spoonOutputDirectory2.exists());
    assertTrue(spoonOutputDirectory2.list().length>0);

  }

}