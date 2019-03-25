package com.bigbass.sandpiles.sim;

import com.badlogic.gdx.math.Vector2;

public class Grid {
	
	public Vector2 pos;
	
	public float[][] cells;
	
	public Grid(float x, float y, int width, int height){
		pos = new Vector2(x, y);
		
		cells = new float[width][height];
		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				cells[i][j] = 0f;
			}
		}
	}
}