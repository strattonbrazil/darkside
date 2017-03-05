/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strattonbrazil.darkside;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 *
 * @author stratton
 */
public class ModelShader extends BaseShader {

    protected final ShaderProgram program;
    protected final Pass pass;

    public enum Pass {
        COLOR_PASS,
        ATTR_PASS
    };

    public ModelShader(Pass pass) {
        super();

        this.pass = pass;
        
        String vert = getVertexShaderByPass(pass);
        String frag = getFragmentShaderByPass(pass);

        program = new ShaderProgram(vert, frag);

        if (!program.isCompiled()) {
            throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
        }
        String log = program.getLog();
        if (log.length() > 0) {
            Gdx.app.error("ShaderTest", "Shader compilation log: " + log);
        }
    }

    @Override
    public void init() {
        super.init(program, null);
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }

    protected final int u_projTrans = register(new Uniform("u_projTrans"));
    protected final int u_worldTrans = register(new Uniform("u_worldTrans"));
    protected final int u_color = register(new Uniform("u_color"));

    @Override
    public void begin(Camera camera, RenderContext context) {
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
        context.setDepthMask(true);
        set(u_projTrans, camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        set(u_worldTrans, renderable.worldTransform);

        if (pass == Pass.COLOR_PASS) {
            ColorAttribute colorAttr = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);

            set(u_color, colorAttr.color);
        }

        renderable.meshPart.render(program);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        program.dispose();
    }

    public static ShaderProvider getShaderProvider(final Pass pass) {
        return new ShaderProvider() {
            Shader shader = null;

            public Shader getShader(Renderable renderable) {
                if (shader == null) {
                    shader = new ModelShader(pass);
                    shader.init();
                }
                return shader;
            }

            public void dispose() {
                shader.dispose();
            }
        };
    }

    private String getVertexShaderByPass(Pass pass) {
        if (pass == Pass.COLOR_PASS) {
            return Gdx.files.internal("first_vertex.glsl").readString();
        } else if (pass == Pass.ATTR_PASS) {
            return Gdx.files.internal("second_vertex.glsl").readString();
        } else {
            throw new GdxRuntimeException("unsupported pass: " + pass);
        }
    }

    private String getFragmentShaderByPass(Pass pass) {
        if (pass == Pass.COLOR_PASS) {
            return Gdx.files.internal("first_fragment.glsl").readString();
        } else if (pass == Pass.ATTR_PASS) {
            return Gdx.files.internal("second_fragment.glsl").readString();
        } else {
            throw new GdxRuntimeException("unsupported pass: " + pass);
        }
    }
}
