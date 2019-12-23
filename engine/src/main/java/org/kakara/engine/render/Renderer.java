package org.kakara.engine.render;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.kakara.engine.Camera;
import org.kakara.engine.collision.BoxCollider;
import org.kakara.engine.gui.Window;
import org.kakara.engine.item.GameItem;
import org.kakara.engine.item.Mesh;
import org.kakara.engine.lighting.PointLight;
import org.kakara.engine.utils.Utils;

import java.util.List;

public class Renderer {
    private Transformation transformation;

    private Shader shaderProgram;

    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.0f;

    private Matrix4f projectionMatrix;

    private float specularPower;

    public Renderer() {
        transformation = new Transformation();
        specularPower = 10f;
    }

    public void init(Window window) throws Exception {
        shaderProgram = new Shader();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        // Create uniform for material
        shaderProgram.createMaterialUniform("material");
        // Create lighting related uniforms
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createPointLightUniform("pointLight");

    }

    public void render(Window window, List<GameItem> gameObjects, Camera camera, Vector3f ambientLight, PointLight pointLight){
        clear();

        if(window.isResized()){
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
        shaderProgram.bind();
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Update Light Uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);
        // Get a copy of the light object and transform its position to view coordinates
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        shaderProgram.setUniform("pointLight", currPointLight);


        shaderProgram.setUniform("texture_sampler", 0);


        for(GameItem gameObject : gameObjects) {
            // Set world matrix for this item
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameObject, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            // Render the mes for this game item
//            mesh.render();
            gameObject.render(shaderProgram);
            /*
                Below is the code for the debug mode for the box collider.
             */
//            if(gameObject.getCollider() instanceof BoxCollider){
//                Matrix4f colliderViewMatrix = new Matrix4f().identity().scale(0.3f).translate(gameObject.getCollider().getAbsolutePoint1().subtract(1, 1, 1).divide(1-gameObject.getScale()).toJoml());
//                Matrix4f viewCurr = new Matrix4f(viewMatrix);
//                Matrix4f curColliderMatrix = viewCurr.mul(colliderViewMatrix);
//                shaderProgram.setUniform("modelViewMatrix", curColliderMatrix);
//                ((BoxCollider) gameObject.getCollider()).render();
//            }
        }

        shaderProgram.unbind();

    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
