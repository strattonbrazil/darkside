package com.strattonbrazil.darkside;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController.Transform;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
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
    MouseHandler mouseHandler;
    BitmapFont font;
    FrameBuffer gameBuffer;
    FrameBuffer blurBuffer;
    
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
        batch.draw(blurBuffer.getColorBufferTexture(), 0, SCREEN_HEIGHT, SCREEN_WIDTH, -SCREEN_HEIGHT); // flip texture right-side up
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
    }
    
    public void renderScene() {
        final int SCREEN_WIDTH = Gdx.graphics.getWidth();
        final int SCREEN_HEIGHT = Gdx.graphics.getHeight();

        if (gameBuffer == null) {
            gameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, SCREEN_WIDTH, SCREEN_HEIGHT, true);
        }

        gameBuffer.begin();
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        ModelBatch modelBatch = new ModelBatch();
        modelBatch.begin(camera);
        modelBatch.render(ship);
        modelBatch.render(boxNegX);
        modelBatch.render(boxPosX);
        modelBatch.render(boxNegY);
        modelBatch.render(boxPosY);
        modelBatch.render(boxNegZ);
        modelBatch.render(boxPosZ);
        modelBatch.end();
        
        gameBuffer.end();
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
        
        String vertexShader = Gdx.files.internal("vertex.glsl").readString();
        String fragmentShader = Gdx.files.internal("fragment.glsl").readString();
        ShaderProgram shaderProgram = new ShaderProgram(vertexShader,fragmentShader);
        
        batch.begin();
        batch.setShader(shaderProgram);
        batch.draw(gameBuffer.getColorBufferTexture(), 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
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
