/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strattonbrazil.darkside;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author stratton
 */
public class MouseHandler implements InputProcessor {
    private boolean _wasRightDown = false;
    private Vector2 _mousePick = new Vector2();
     
    public boolean isNavigating() {
        return Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
    }
    
    public Vector2 mousePos() {
        return new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }
    
    public Vector2 mousePick() {
        Vector2 flip = _mousePick.cpy();
        flip.y = Gdx.graphics.getHeight() - flip.y; // flip to y-up
        return flip;
    }
    
    public Vector2 mouseDelta() {
        return mousePos().cpy().sub(_mousePick);
    }
    
    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        
        
        return true;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        boolean isRightDown = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        if (!isRightDown) {
            _mousePick.x = Gdx.input.getX();
            _mousePick.y = Gdx.input.getY();
        }
        
        return true;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }
    
}
