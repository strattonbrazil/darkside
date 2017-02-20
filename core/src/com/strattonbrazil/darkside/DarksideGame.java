package com.strattonbrazil.darkside;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.BaseAnimationController.Transform;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class DarksideGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    Camera camera;
    Transform shipRef;
    ModelInstance ship;
    ModelInstance box;
    MouseHandler mouseHandler;
    BitmapFont font;

    @Override
    public void create() {
        mouseHandler = new MouseHandler();
        Gdx.input.setInputProcessor(mouseHandler);
        
        font = new BitmapFont();
        
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
     
        shipRef = new Transform();
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal);
        ship = new ModelInstance(model);
        
        model = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.BLUE)), Usage.Position | Usage.Normal);
        box = new ModelInstance(model);
    }

    @Override
    public void render() {
        update();
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        ModelBatch modelBatch = new ModelBatch();
        modelBatch.begin(camera);
        modelBatch.render(ship);
        modelBatch.render(box);
        modelBatch.end();
        
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
            
            // turn left/right
            Vector3 upDir = new Vector3(0, 1, 0);
            float leftDegreeDelta = mouseDelta.x * -0.01f; // TODO: cap and take screensize into account
            shipRefRotation.transform(upDir);
            float newUpAngle = shipRefRotation.getAngleAround(upDir) + leftDegreeDelta;
            shipRefRotation.setFromAxis(upDir, newUpAngle); // turn left
            
            // turn up/down
            Vector3 rightDir = new Vector3(1, 0, 0);
            float upDegreeDelta = mouseDelta.y * 0.01f; // TODO: cap and take screensize into account
            shipRefRotation.transform(rightDir);
            float newRightAngle = shipRefRotation.getAngleAround(rightDir) + upDegreeDelta;
            shipRefRotation.setFromAxis(rightDir, newRightAngle);
            
            shipRef.set(shipRef.translation, shipRefRotation, shipRef.scale);
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
        camera.far = 300f;
        camera.update();        
        
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }    
}
