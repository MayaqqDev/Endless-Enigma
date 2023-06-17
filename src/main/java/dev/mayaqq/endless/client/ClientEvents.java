package dev.mayaqq.endless.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ClientEvents {

    static boolean cutSceneInProgress = false;
    static int cutScenePhase = 0;

    static int renderTick = 0;
    static ArrayList<String> cutSceneBuffer = new ArrayList<>();
    static String text = "";
    static int textPhase = 0;
    static boolean shouldContinue = true;
    static String fullText = "";

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS && cutSceneInProgress) {
                cutSceneBuffer.clear();
                cutSceneInProgress = false;
                cutScenePhase = 0;
                text = "";
                textPhase = 0;
                shouldContinue = true;
            }
            if (client.options.hudHidden && cutSceneInProgress) {
                client.options.hudHidden = false;
            }
            if (KeybindRegistry.cutscene.wasPressed() && cutSceneInProgress) {
                cutScenePhase++;
            }
        });
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            if (cutSceneBuffer.size() > 0) {
                fullText = cutSceneBuffer.get(0);
                cutSceneInProgress = true;
            }
            if (cutSceneInProgress) {
                renderTick++;
                if (renderTick == 10) {
                    renderTick = 0;
                }
                MinecraftClient client = MinecraftClient.getInstance();
                client.options.hudHidden = false;

                int windowWidth = client.getWindow().getScaledWidth();
                int windowHeight = client.getWindow().getScaledHeight();

                int white = 0xFFFFFFFF;
                int black = 0x80000000;

                client.getFramebuffer().beginWrite(false);
                RenderSystem.enableDepthTest();

                TextRenderer textRenderer = client.textRenderer;
                MatrixStack matrixStack = matrices.getMatrices();
                matrixStack.push();
                matrixStack.translate(0, 0, 1000);

                matrices.fill(0, 0, windowWidth, windowHeight, black);
                if (renderTick == 0 && shouldContinue) {
                    if (textPhase == fullText.length()) {
                        shouldContinue = false;
                    } else {
                        text += fullText.charAt(textPhase);
                        textPhase++;
                    }
                }
                matrices.drawCenteredTextWithShadow(textRenderer, text, windowWidth / 2, windowHeight / 2 - 20, white);
                String pressText = "Press " + KeybindRegistry.cutscene.getBoundKeyLocalizedText().getString() + " to continue";
                matrices.drawText(
                        textRenderer,
                        pressText,
                        windowWidth - textRenderer.getWidth(pressText) - 5,
                        windowHeight - textRenderer.fontHeight - 5,
                        white,
                        true
                );

                matrixStack.pop();
                RenderSystem.disableDepthTest();

                client.getFramebuffer().endWrite();
                if (cutScenePhase == 1) {
                    cutSceneBuffer.remove(0);
                    cutSceneInProgress = false;
                    cutScenePhase = 0;
                    text = "";
                    textPhase = 0;
                    shouldContinue = true;
                }
            }
        });
    }

    public static void renderTextCutScene(String text) {
        cutSceneBuffer.add(text);
    }
}