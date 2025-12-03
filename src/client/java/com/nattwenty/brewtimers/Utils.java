package com.nattwenty.brewtimers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class Utils {
    public static final int[] lastViewport = new int[4];

    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.getEntityRenderDispatcher().camera;
        int displayHeight = client.getWindow().getHeight();
        Vector3f target = new Vector3f();

        assert camera != null;
        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Matrix4f matrixProj = new Matrix4f(client.gameRenderer.getBasicProjectionMatrix(client.options.getFov().getValue()));
        Matrix4f matrixModel = new Matrix4f(RenderSystem.getModelViewMatrix());
        Quaternionf quaternionf = camera.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix4f3 = (new Matrix4f()).rotation(quaternionf);

        //client.worldRenderer.setupFrustum(camera.getPos(), matrix4f3, matrixProj);

        MatrixStack matrix = new MatrixStack();
        matrix.multiplyPositionMatrix(matrix4f3);

        Vector4f transformedCoordinates = new Vector4f((float)deltaX, (float)deltaY, (float)deltaZ, 1.0f).mul(matrix.peek().getPositionMatrix());
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, Utils.lastViewport);

        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), lastViewport, target);

        return new Vec3d(target.x / client.getWindow().getScaleFactor(), (displayHeight - target.y) / client.getWindow().getScaleFactor(), target.z);
    }
}
