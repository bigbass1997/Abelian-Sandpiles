package com.bigbass.sandpiles.sim;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class Simulation {
	
	public Vector2 pos;
	public Vector2 dim;

	private Grid gridActive;
	//private Grid gridTemp;
	
	private int stepsPerFrame = 8; // Decrease if FPS is too low. Controls the number of generations per frame
	
	private int generations = 0;
	
	private boolean isRendering = false;
	
	private ForkJoinPool pool;
	
	public Simulation(float x, float y, float width, float height){
		pos = new Vector2(x, y);
		dim = new Vector2(width, height);
		
		gridActive = new Grid(x, y, (int) width, (int) height);
		//gridTemp = new Grid(x, y, (int) width, (int) height);
		
		gridActive.cells[128][128].val = 100000;
		//gridTemp.cells[128][128].val = 20;
		
		pool = ForkJoinPool.commonPool();
	}
	
	public void updateAndRender(ShapeRenderer sr, Camera cam){
		isRendering = !Gdx.input.isKeyPressed(Keys.SPACE); // Disable rendering by holding SPACE
		
		if(isRendering){
			sr.begin(ShapeType.Filled);
		}
		for(int z = 0; z < stepsPerFrame; z++){
			
			if(Gdx.input.isKeyPressed(Keys.T) || Gdx.input.isKeyJustPressed(Keys.Y)){
				Grid nextGrid = new Grid(pos.x, pos.y, (int) dim.x, (int) dim.y);
				
				for (int i = 1; i < gridActive.cells.length - 1; i++) {
					for (int j = 1; j < gridActive.cells[i].length - 1; j++) {
						final int val = gridActive.cells[i][j].val;
						Cell next = nextGrid.cells[i][j];
						
						next.val = 0;
						if(val < 4){
							next.val = val;
						}
					}
				}
				/*UpdateWorld wu = new UpdateWorld(gridTemp, gridActive, 1, gridTemp.cells.length - 1, 1, gridTemp.cells[1].length - 1);
				pool.execute(wu);
				wu.join();*/
				

				for (int i = 1; i < gridActive.cells.length - 1; i++) {
					for (int j = 1; j < gridActive.cells[i].length - 1; j++) {
						final int v = gridActive.cells[i][j].val;
						Cell t = nextGrid.cells[i][j];
						
						if(v >= 4){
							t.val += v - 4;
							nextGrid.cells[i - 1][j].val += 1;
							nextGrid.cells[i + 1][j].val += 1;
							nextGrid.cells[i][j - 1].val += 1;
							nextGrid.cells[i][j + 1].val += 1;
						}
					}
				}
				
				
				
				// Copy gridTemp data into gridActive
				/*Grid swap = gridActive;
				gridActive = gridTemp;
				gridTemp = swap;*/
				
				gridActive = nextGrid;
				
				//System.out.println(gridActive.cells[128][128].val);

				generations += 1;
			}
			
			if(isRendering && z == stepsPerFrame - 1){
				for (int i = 1; i < gridActive.cells.length - 1; i++) {
					for (int j = 1; j < gridActive.cells[i].length - 1; j++) {
						Cell c = gridActive.cells[i][j];
						final int val = c.val;
						
						switch(val){
						case 0:
							sr.setColor(1, 1, 1, 1);
							break;
						case 1:
							sr.setColor(0.7f, 0.7f, 0.7f, 1);
							break;
						case 2:
							sr.setColor(0.4f, 0.4f, 0.4f, 1);
							break;
						default:
							sr.setColor(0.1f, 0.1f, 0.1f, 1);
							break;
						}
						sr.rect(gridActive.pos.x + i, gridActive.pos.y + j, 1, 1);
					}
				}
				
			}
		}
		
		if(isRendering){
			sr.end();
		}
	}
	
	public int getGenerations(){
		return generations;
	}
	
	@SuppressWarnings("serial")
	private class UpdateWorld extends RecursiveAction {
		private int threshold = 1000;
		private Grid tmp;
		private Grid active;
		private int startx;
		private int endx;
		private int starty;
		private int endy;

		public UpdateWorld(Grid tmp, Grid active, int startx, int endx, int starty, int endy) {
			this.tmp = tmp;
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

				UpdateWorld uwA = new UpdateWorld(tmp, active, startx, startx + xdiff, starty, starty + ydiff);
				UpdateWorld uwB = new UpdateWorld(tmp, active, startx + xdiff, endx, starty, starty + ydiff);
				UpdateWorld uwC = new UpdateWorld(tmp, active, startx, startx + xdiff, starty + ydiff, endy);
				UpdateWorld uwD = new UpdateWorld(tmp, active, startx + xdiff, endx, starty + ydiff, endy);

				invokeAll(uwA, uwB, uwC, uwD);
			} else {
				for (int i = startx; i < endx; i++) {
					for (int j = starty; j < endy; j++) {
						Cell c = active.cells[i][j];
						Cell t = tmp.cells[i][j];
						
						int v = c.val;
						if(v >= 4){
							t.val = v - 4;
							tmp.cells[i - 1][j].val += 1;
							tmp.cells[i + 1][j].val += 1;
							tmp.cells[i][j - 1].val += 1;
							tmp.cells[i][j + 1].val += 1;
						}
					}
				}
			}
		}
	}
}