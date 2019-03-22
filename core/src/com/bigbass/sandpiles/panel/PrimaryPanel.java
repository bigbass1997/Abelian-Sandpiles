package com.bigbass.sandpiles.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bigbass.sandpiles.Main;
import com.bigbass.sandpiles.sim.Simulation;
import com.bigbass.sandpiles.skins.SkinManager;

public class PrimaryPanel extends Panel {

	private final Simulation SIM;
	
	private final float CAM_SPEED = 400;
	
	private Camera cam;
	private Stage stage;
	private ShapeRenderer sr;
	
	private Label infoLabel;
	
	private float scalar = 1;
	
	public PrimaryPanel() {
		super();
		
		final int SIZE = 256;
		SIM = new Simulation(0, 0, SIZE, SIZE);
		
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(SIM.dim.x * 0.5f, SIM.dim.y * 0.5f, 0);
		cam.update();
		
		stage = new Stage();
		Main.inputMultiplexer.addProcessor(stage);
		Main.inputMultiplexer.addProcessor(new ScrollwheelInputAdapter(){
			@Override
			public boolean scrolled(int amount) {
				if(amount == 1){
					changeCameraViewport(1);
				} else if(amount == -1){
					changeCameraViewport(-1);
				}
				return true;
			}
		});
		
		infoLabel = new Label("", SkinManager.getSkin("fonts/droid-sans-mono.ttf", 10));
		infoLabel.setColor(Color.MAGENTA);
		stage.addActor(infoLabel);
		
		sr = new ShapeRenderer(1048576);
		sr.setAutoShapeType(true);
		sr.setProjectionMatrix(cam.combined);
	}
	
	public void render() {
		SIM.updateAndRender(sr, cam);
		
		panelGroup.render();
		
		stage.draw();
		
		/*sr.begin(ShapeType.Filled);
		sr.setColor(Color.FIREBRICK);
		renderDebug(sr);
		sr.end();*/
	}
	
	public void update(float delta) {
		panelGroup.update(delta);
		
		stage.act(delta);
		
		String info = String.format("FPS: %s%nGens: %s",
				Gdx.graphics.getFramesPerSecond(),
				SIM.getGenerations()
			);
		
		infoLabel.setText(info);
		infoLabel.setPosition(10, Gdx.graphics.getHeight() - (infoLabel.getPrefHeight() / 2) - 5);
		
		Input input = Gdx.input;
		boolean dirty = false;
		if(input.isKeyPressed(Keys.W)){
			cam.translate(0, CAM_SPEED * delta, 0);
			dirty = true;
		}
		if(input.isKeyPressed(Keys.S)){
			cam.translate(0, -CAM_SPEED * delta, 0);
			dirty = true;
		}
		if(input.isKeyPressed(Keys.A)){
			cam.translate(-CAM_SPEED * delta, 0, 0);
			dirty = true;
		}
		if(input.isKeyPressed(Keys.D)){
			cam.translate(CAM_SPEED * delta, 0, 0);
			dirty = true;
		}
		if(dirty){
			cam.update();
			sr.setProjectionMatrix(cam.combined);
			dirty = false;
		}
	}
	
	public boolean isActive() {
		return true; // Always active
	}
	
	public void dispose(){
		stage.dispose();
		sr.dispose();
		panelGroup.dispose();
	}
	
	private void changeCameraViewport(int dscalar){
		scalar += dscalar / 10f;
		
		cam.viewportWidth = Gdx.graphics.getWidth() * scalar;
		cam.viewportHeight = Gdx.graphics.getHeight() * scalar;
		cam.update();
		
		sr.setProjectionMatrix(cam.combined);
	}
}
