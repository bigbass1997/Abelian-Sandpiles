package com.bigbass.sandpiles.sim;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import io.anuke.gif.GifRecorder;

public class Simulation {
	
	public Vector2 pos;
	public Vector2 dim;

	private Grid gridActive;
	private Grid gridTemp;
	
	private final int TOPPLE_SIZE = 4;
	
	private int stepsPerFrame = 14; // Decrease if FPS is too low. Controls the number of generations per frame
	
	private int generations = 0;
	private int renderFrames = 0;
	
	private boolean isRendering = false;
	
	private ForkJoinPool pool;
	
	/*private final Color ZERO = new Color(0x000000FF);
	private final Color ONE = new Color(0xFF0000FF);
	private final Color TWO = new Color(0xFFA500FF);
	private final Color THREE = new Color(0xFFFF00FF);*/

	private final Color ZERO = (new Color(0x000000FF)).fromHsv(0, 0, 0);
	private final Color ONE = (new Color(0x000000FF)).fromHsv(80, 0, 1.00f);
	private final Color TWO = (new Color(0x000000FF)).fromHsv(160, 0, 0.75f);
	private final Color THREE = (new Color(0x000000FF)).fromHsv(240, 0, 0.50f);
	private final Color FOURMORE = (new Color(0x000000FF)).fromHsv(320, 0, 0.25f);

	private SpriteBatch gifBatch;
	private GifRecorder gifRecorder;
	
	public Simulation(float x, float y, float width, float height){
		pos = new Vector2(x, y);
		dim = new Vector2(width, height);
		
		gridActive = new Grid(x, y, (int) width, (int) height);
		gridTemp = new Grid(x, y, (int) width, (int) height);
		
		gridActive.cells[512][512] = 200000;
		//gridActive.cells[512 + 16][512 - 5] = 200000;
		
		pool = ForkJoinPool.commonPool();

		gifBatch = new SpriteBatch();
		gifRecorder = new GifRecorder(gifBatch);
		gifRecorder.open();
		gifRecorder.setBounds(-width, -height + 42, (width * 2) - 1, (height * 2) - 84);
		gifRecorder.setFPS(20);
		//gifRecorder.startRecording(); // Remove this comment to try recording
		gifRecorder.setRecordKey(-2);
	}
	
	public void updateAndRender(ShapeRenderer sr, Camera cam){
		isRendering = !Gdx.input.isKeyPressed(Keys.SPACE); // Disable rendering by holding SPACE
		
		if(isRendering){
			sr.begin(ShapeType.Line);
			sr.setColor(0.4f, 0.4f, 0.4f, 1);
			sr.rect(pos.x, pos.y, dim.x, dim.y);
			
			sr.set(ShapeType.Line);
		}
		for(int z = 0; z < stepsPerFrame; z++){
			
			for (int i = 1; i < gridActive.cells.length - 1; i++) {
				for (int j = 1; j < gridActive.cells[i].length - 1; j++) {
					final float val = gridActive.cells[i][j];
					
					gridTemp.cells[i][j] = 0;
					if(val < TOPPLE_SIZE){
						gridTemp.cells[i][j] = val;
					}
				}
			}
			UpdateWorld wu = new UpdateWorld(gridTemp, gridActive, 1, gridTemp.cells.length - 1, 1, gridTemp.cells[1].length - 1);
			pool.execute(wu);
			wu.join();
			
			// Copy gridTemp data into gridActive
			Grid swap = gridActive;
			gridActive = gridTemp;
			gridTemp = swap;

			generations += 1;
			
			
			// RENDER SECTION \\
			if(isRendering && z == stepsPerFrame - 1){
				for (int i = 1; i < gridActive.cells.length - 1; i++) {
					for (int j = 1; j < gridActive.cells[i].length - 1; j++) {
						final float val = gridActive.cells[i][j];
						
						switch((int) val){
						case 0:
							break;
						case 1:
							sr.setColor(ONE);
							sr.point(gridActive.pos.x + i, gridActive.pos.y + j, 0);
							break;
						case 2:
							sr.setColor(TWO);
							sr.point(gridActive.pos.x + i, gridActive.pos.y + j, 0);
							break;
						case 3:
							sr.setColor(THREE);
							sr.point(gridActive.pos.x + i, gridActive.pos.y + j, 0);
							break;
						default:
							sr.setColor(FOURMORE);
							sr.point(gridActive.pos.x + i, gridActive.pos.y + j, 0);
							break;
						}
					}
				}
				
			}
		}
		
		if(isRendering){
			sr.end();
			
			if(gifRecorder.isRecording() && renderFrames % 2 == 0){ // record new frame every n generations
				//gifRecorder.setFPS(Gdx.graphics.getFramesPerSecond() < 15 ? 15 : Gdx.graphics.getFramesPerSecond() - 5);
				gifRecorder.update();
			}
			
			renderFrames += 1;
		}
		
		if(gifRecorder.isRecording() && Gdx.input.isKeyPressed(Keys.L)){
			gifRecorder.finishRecording();
			gifRecorder.writeGIF();
		}
	}
	
	public int getGenerations(){
		return generations;
	}
	
	@SuppressWarnings("serial")
	private class UpdateWorld extends RecursiveAction {
		private int threshold = 500;
		private Grid nextGrid;
		private Grid active;
		private int startx;
		private int endx;
		private int starty;
		private int endy;

		public UpdateWorld(Grid nextGrid, Grid active, int startx, int endx, int starty, int endy) {
			this.nextGrid = nextGrid;
			this.active = active;
			this.startx = startx;
			this.endx = endx;
			this.starty = starty;
			this.endy = endy;
		}

		public void compute() {
			int work = (endx - startx) * (endy - starty);
			if (work > threshold) {
				int xdiff = (endx - startx) / 2;
				int ydiff = (endy - starty) / 2;

				UpdateWorld uwA = new UpdateWorld(nextGrid, active, startx, startx + xdiff, starty, starty + ydiff);
				UpdateWorld uwB = new UpdateWorld(nextGrid, active, startx + xdiff, endx, starty, starty + ydiff);
				UpdateWorld uwC = new UpdateWorld(nextGrid, active, startx, startx + xdiff, starty + ydiff, endy);
				UpdateWorld uwD = new UpdateWorld(nextGrid, active, startx + xdiff, endx, starty + ydiff, endy);

				invokeAll(uwA, uwB, uwC, uwD);
			} else {
				for (int i = startx; i < endx; i++) {
					for (int j = starty; j < endy; j++) {
						final float v = gridActive.cells[i][j];
						
						if(v >= TOPPLE_SIZE){
							nextGrid.cells[i][j] += v - TOPPLE_SIZE;
							nextGrid.cells[i - 1][j] += TOPPLE_SIZE / 4f;
							nextGrid.cells[i + 1][j] += TOPPLE_SIZE / 4f;
							nextGrid.cells[i][j - 1] += TOPPLE_SIZE / 4f;
							nextGrid.cells[i][j + 1] += TOPPLE_SIZE / 4f;
						}
					}
				}
			}
		}
	}
}