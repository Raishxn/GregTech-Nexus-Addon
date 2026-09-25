package com.raishxn.gtna.gametest;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.LogTestReporter;
import net.minecraft.gametest.framework.TestReporter;
import net.minecraftforge.fml.loading.FMLPaths;

import com.raishxn.gtna.GTNACORE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;

/** Writes one JUnit case per GameTest while preserving Minecraft's normal failure logging. */
public final class GTNAGameTestReport {

    private GTNAGameTestReport() {}

    public static void install() {
        Path gameDirectory = FMLPaths.GAMEDIR.get().toAbsolutePath().normalize();
        Path report = gameDirectory.resolve("../build/test-results/gametest/TEST-gtna.xml").normalize();
        try {
            Files.createDirectories(report.getParent());
            JUnitLikeTestReporter junit = new JUnitLikeTestReporter(report.toFile());
            LogTestReporter log = new LogTestReporter();
            GlobalTestReporter.replaceWith(new TestReporter() {

                @Override
                public void onTestFailed(GameTestInfo test) {
                    log.onTestFailed(test);
                    junit.onTestFailed(test);
                }

                @Override
                public void onTestSuccess(GameTestInfo test) {
                    log.onTestSuccess(test);
                    junit.onTestSuccess(test);
                }

                @Override
                public void finish() {
                    junit.finish();
                    GTNACORE.LOGGER.info("GameTest JUnit report: {}", report);
                }
            });
        } catch (IOException | ParserConfigurationException error) {
            throw new IllegalStateException("Could not initialize GameTest JUnit report at " + report, error);
        }
    }
}
