package com.strattonbrazil.darkside;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController.Transform;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FloatFrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class DarksideGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    Camera camera;
    Transform shipRef;
    ModelInstance ship;
    ModelInstance boxNegX;
    ModelInstance boxPosX;
    ModelInstance boxNegY;
    ModelInstance boxPosY;
    ModelInstance boxNegZ;
    ModelInstance boxPosZ;
    ModelInstance sphere;
    MouseHandler mouseHandler;
    BitmapFont font;
    FrameBuffer gBuffer1; // color
    FrameBuffer gBuffer2; // normals
    FrameBuffer blurBuffer;
    ModelBatch modelColorPass;
    ModelBatch modelAttrPass;
    FPSLogger fpsLogger = new FPSLogger();
    
    @Override
    public void create() {
        mouseHandler = new MouseHandler();
        Gdx.input.setInputProcessor(mouseHandler);
        
        font = new BitmapFont();
        
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
     
        shipRef = new Transform();
        camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.TAN)), Usage.Position | Usage.Normal);
        ship = new ModelInstance(model);
        
        final float SIZE = 1000f;
        model = modelBuilder.createBox(1f, SIZE, SIZE, new Material(ColorAttribute.createDiffuse(Color.MAGENTA)), Usage.Position | Usage.Normal);
        boxNegX = new ModelInstance(model);
        boxNegX.transform.translate(-SIZE, 0, 0);
        boxPosX = new ModelInstance(model);
        boxPosX.transform.translate(SIZE, 0, 0);
        model = modelBuilder.createBox(SIZE, 1f, SIZE, new Material(ColorAttribute.createDiffuse(Color.BLUE)), Usage.Position | Usage.Normal);
        boxNegY = new ModelInstance(model);
        boxNegY.transform.translate(0, -SIZE, 0);
        boxPosY = new ModelInstance(model);
        boxPosY.transform.translate(0, SIZE, 0);
        model = modelBuilder.createBox(SIZE, SIZE, 1f, new Material(ColorAttribute.createDiffuse(Color.CHARTREUSE)), Usage.Position | Usage.Normal);
        boxNegZ = new ModelInstance(model);
        boxNegZ.transform.translate(0, 0, -SIZE);
        boxPosZ = new ModelInstance(model);
        boxPosZ.transform.translate(0, 0, SIZE);
        
        model = modelBuilder.createSphere(100, 100, 100, 50, 50, new Material(ColorAttribute.createDiffuse(Color.LIME)), Usage.Position | Usage.Normal);
        sphere = new ModelInstance(model);
        sphere.transform.translate(0, 0, SIZE);
        
        modelColorPass = new ModelBatch(ModelShader.getShaderProvider(ModelShader.Pass.COLOR_PASS));
        modelAttrPass = new ModelBatch(ModelShader.getShaderProvider(ModelShader.Pass.ATTR_PASS));
    }

    @Override
    public void render() {
        update();

        final int SCREEN_WIDTH = Gdx.graphics.getWidth();
        final int SCREEN_HEIGHT = Gdx.graphics.getHeight();
        
        renderScene(); // renders scene to game buffer
        renderMotionBlur();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        batch.begin();
        //batch.draw(blurBuffer.getColorBufferTexture(), 0, SCREEN_HEIGHT, SCREEN_WIDTH, -SCREEN_HEIGHT); // flip texture right-side up
        batch.draw(blurBuffer.getColorBufferTexture(), 0, 0);
        batch.end();
        
        if (mouseHandler.isNavigating()) {
            Vector2 mousePick = mouseHandler.mousePick();
            batch.begin();
            
            font.draw(batch, "X", mousePick.x, mousePick.y);
            Vector2 mousePos = mouseHandler.mousePos();
            for (int i = 1; i <= 3; i++) {
                Vector2 dotPos = mousePick.lerp(mousePos, i/3.0f);
                String s = ".";
                if (i == 3)
                    s = "O";
                font.draw(batch, s, dotPos.x, dotPos.y);
            }
            batch.end();
        }
        
        //fpsLogger.log();
    }
    
    public void renderScene() {
        final int SCREEN_WIDTH = Gdx.graphics.getWidth();
        final int SCREEN_HEIGHT = Gdx.graphics.getHeight();

        if (gBuffer1 == null) {
            gBuffer1 = new FloatFrameBuffer(SCREEN_WIDTH, SCREEN_HEIGHT, true);
        }
        if (gBuffer2 == null) {
            gBuffer2 = new FloatFrameBuffer(SCREEN_WIDTH, SCREEN_HEIGHT, true);
        }

        renderScenePass(gBuffer1, modelColorPass);
    }
    
    private void renderScenePass(FrameBuffer buffer, ModelBatch pass) {
        buffer.begin();
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        pass.begin(camera);
        pass.render(ship);
        pass.render(boxNegX);
        pass.render(boxPosX);
        pass.render(boxNegY);
        pass.render(boxPosY);
        pass.render(boxNegZ);
        pass.render(boxPosZ);
        pass.render(sphere);
        pass.end();
        
        buffer.end();
    }
    
    public void renderMotionBlur() {
        final int SCREEN_WIDTH = Gdx.graphics.getWidth();
        final int SCREEN_HEIGHT = Gdx.graphics.getHeight();
        
        if (blurBuffer == null) {
            blurBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, SCREEN_WIDTH, SCREEN_HEIGHT, true);
            
        }
        
        blurBuffer.begin();
        
        // this is convenient for full-screen quads
        batch = new SpriteBatch();
        
        String vertexShader = Gdx.files.internal("post_vertex.glsl").readString();
        String fragmentShader = Gdx.files.internal("post_fragment.glsl").readString();
        ShaderProgram shaderProgram = new ShaderProgram(vertexShader,fragmentShader);
        
        Vector2 blurDir = new Vector2(0,0);
        if (mouseHandler.isNavigating()) {
            blurDir.x = 0.0002f * mouseHandler.mouseDelta().x;
            blurDir.y = 0.0002f * mouseHandler.mouseDelta().y;
        }
        
        batch.begin();
        batch.setShader(shaderProgram);
        shaderProgram.setUniformf("blurDir", blurDir.x, blurDir.y);
        batch.draw(gBuffer1.getColorBufferTexture(), 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.setShader(null); // reset to default
        batch.end();
        
        blurBuffer.end();
    }
    
    public void update() {
        // update the base position
        //
        
        // get ship rotation
        Quaternion shipRefRotation = shipRef.rotation.cpy();
        
        // set ship rotation based on input
        Vector3 forwardDir = new Vector3(0, 0, -1.0f); // dir of ship if going forward
        shipRefRotation.transform(forwardDir);
        if (mouseHandler.isNavigating()) {
            Vector2 mouseDelta = mouseHandler.mouseDelta();
            
            //System.out.println(mouseDelta);
            
            // turn left/right
            Vector3 upDir = new Vector3(0, 1, 0);
            shipRef.rotation.transform(upDir);
            Quaternion leftRight = new Quaternion(upDir, mouseDelta.x * -0.01f);
            shipRef.rotation.mulLeft(leftRight);
            
            // turn up/down
            Vector3 rightDir = new Vector3(1, 0, 0);
            shipRefRotation.transform(rightDir);
            Quaternion upDown = new Quaternion(rightDir, mouseDelta.y * 0.01f);
            shipRef.rotation.mulLeft(upDown);
        }
        
        // update reference location
        shipRef.translation.mulAdd(forwardDir, 0.1f);
        
        // update ship position
        shipRef.toMatrix4(ship.transform);
        
        // update the camera
        Vector3 offset = shipRef.translation.cpy();
        offset.mulAdd(forwardDir, -10);
        Vector3 upDir = new Vector3(0, 1, 0);
        shipRefRotation.transform(upDir);
        offset.mulAdd(upDir, 3);
        camera.position.set(offset);
        camera.lookAt(shipRef.translation);
        camera.up.set(upDir);
        camera.near = 1f;
        camera.far = 3000f;
        camera.update();        
        
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }    
}
