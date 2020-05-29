package com.mygdx.game;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class RainyDay extends ApplicationAdapter {

	// Instance Variables
	private Texture dropImage;
	private Texture bucketImage;
	private Texture backgroundImage;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private long lastDropTime;
	private BitmapFont font;
	private int score;
	private int highScore;

	// set up the bucket image as well as its hitbox/Rectangle. Also initialize an orthographic camera to view the game screen
	// and to keep track of when the bucket leaves the camera's view in order to keep it bounded in the game screen (as well as drops)
	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		backgroundImage = new Texture(Gdx.files.internal("rain_drops_streaksstockphotopng.jpeg"));

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();
		font = new BitmapFont();

		// create a Rectangle to logically represent the bucket
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
		bucket.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
		bucket.width = 64;
		bucket.height = 64;

		// create the raindrops array and spawn the first raindrop
		raindrops = new Array<>();
		spawnRaindrop();
	}

	// Spawn a raindrop after a certain amount of rime in a random x location within the screens bounds and a set y location
	// A raindrop is a rectangle with an image of a drop over it
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	// render the camera and all rectangles to the screen including the background, and raindrops.
	// It will also update the location of the bucket on the screen depending on whether "wasd" is being used or to the
	// mouse location.
	@Override
	public void render() {
		// clear the screen with a dark blue color. The
		// arguments to glClearColor are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops
		batch.begin();
		batch.draw(backgroundImage,0,0);
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		font.draw(batch,"score: " + score, 800-128, 480-64);
		font.draw(batch, "highscore: " + highScore, 800-128, 480-128);
		batch.end();

		// process user input
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}
		if(Gdx.input.isKeyPressed(Keys.A)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.D)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		// make sure the bucket stays within the screen bounds
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		// check if we need to create a new raindrop
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the latter case we play back
		// a sound effect as well.
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			if(score < 10) raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			else if(score < 20) raindrop.y -= 250 * Gdx.graphics.getDeltaTime();
			else if(score < 30) raindrop.y -= 300 * Gdx.graphics.getDeltaTime();
			else if(score < 40) raindrop.y -= 350 * Gdx.graphics.getDeltaTime();
			else if(score < 50) raindrop.y -= 500 * Gdx.graphics.getDeltaTime();
			else raindrop.y -= 700 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) {
				iter.remove();
				if(score > highScore) {
					highScore = score;
				}
				score = 0;
			}
			if(raindrop.overlaps(bucket)) {
				iter.remove();
				score++;
			}
		}
	}

	// dispose of the SpriteBatch, BitMapFont, and images when the game closes
	@Override
	public void dispose() {
		// dispose of all the native resources
		dropImage.dispose();
		bucketImage.dispose();
		/*
		dropSound.dispose();
		rainMusic.dispose();
		*/
		batch.dispose();
		font.dispose();
	}
}