package org.secuso.privacyfriendlybattleships;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import org.secuso.privacyfriendlybattleships.game.Direction;
import org.secuso.privacyfriendlybattleships.game.GameActivityLayoutProvider;
import org.secuso.privacyfriendlybattleships.game.GameCell;
import org.secuso.privacyfriendlybattleships.game.GameController;
import org.secuso.privacyfriendlybattleships.game.GameMode;
import org.secuso.privacyfriendlybattleships.game.GameShip;

/**
 * Created by Alexander Müller on 21.02.2017.
 */

public class PlaceShipActivity extends BaseActivity {

    private SharedPreferences preferences = null;
    private GameController controller;
    private int gridSize;
    private GameActivityLayoutProvider layoutProvider;
    private GridView gridView;
    private GameGridAdapter gridAdapter;
    private GameShip selectedShip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPreferences();
        setContentView(R.layout.activity_place_ship);

        // Get the parameters from the MainActivity or the PlaceShipActivity and initialize the game
        Intent intentIn = getIntent();
        this.controller = intentIn.getParcelableExtra("controller");
        this.gridSize = controller.getGridSize();

        layoutProvider = new GameActivityLayoutProvider(this, this.gridSize);

        setupGridView(this.gridSize);
    }


    protected void setupGridView(int size){
        // Get the grid views of the respective XML-files
        gridView = (GridView) findViewById(R.id.placement_gridview);

        // Set the background color of the grid
        gridView.setBackgroundColor(Color.GRAY);

        // Set the columns of the grid
        gridView.setNumColumns(this.gridSize);

        // Set the layout of the grids
        final ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams) gridView.getLayoutParams();

        marginLayoutParams.setMargins(
                layoutProvider.getMarginLeft(),
                layoutProvider.getMargin(),
                layoutProvider.getMarginRight(),
                0);

        gridView.setLayoutParams(marginLayoutParams);
        gridView.setHorizontalSpacing(1);
        gridView.setVerticalSpacing(1);

        // Initialize the grid for player one
        gridAdapter = new GameGridAdapter(this, this.layoutProvider, this.controller, true, true);
        gridView.setAdapter(gridAdapter);


        // Define the listener for the big grid view, such that it is possible to click on it. When
        // clicking on that grid, the corresponding cell should be yellow.
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int column = i % gridSize;
                int row = i / gridSize;

                GameCell[] shipsCells;
                if(selectedShip != null) {
                    //mark ships cells not highlighted
                    unhighlightCells(selectedShip.getShipsCells());
                }

                GameCell selectedCell = controller.getCurrentGrid().getCell(column, row);
                selectedShip = controller.getCurrentGrid().getShipSet().findShipContainingCell(selectedCell);

                //highlight ships cells
                if ( selectedShip != null )
                    highlightCells(selectedShip.getShipsCells());

                gridAdapter.notifyDataSetChanged();
            }
        });

    }

    private void highlightCells(GameCell[] cells) {
        for( GameCell cell : cells ) {
            int col = cell.getCol();
            int row = cell.getRow();
            this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(
                    gridAdapter.context.getResources().getColor(R.color.yellow)
            );
        }
    }

    private void unhighlightCells(GameCell[] cells) {
        for( GameCell cell : cells ) {
            int col = cell.getCol();
            int row = cell.getRow();
            int shipsOnCell = this.controller.getCurrentGrid().getShipSet().shipsOnCell(cell);
            if (shipsOnCell == 0) {
                this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(Color.WHITE);
            } else if (shipsOnCell == 1) {
                this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(Color.BLACK);
            } else if (shipsOnCell >= 2) {
                this.gridView.getChildAt( row * this.gridSize + col ).setBackgroundColor(
                        gridAdapter.context.getResources().getColor(R.color.red));
            }
        }
    }

    public void onClickButton(View view) {
        if ( this.selectedShip == null )
            return;

        GameCell[] oldCells = this.selectedShip.getShipsCells();
        switch (view.getId()) {
            case R.id.arrow_right:
                this.selectedShip.moveShip(Direction.EAST);
                break;
            case R.id.arrow_left:
                this.selectedShip.moveShip(Direction.WEST);
                break;
            case R.id.arrow_up:
                this.selectedShip.moveShip(Direction.NORTH);
                break;
            case R.id.arrow_down:
                this.selectedShip.moveShip(Direction.SOUTH);
                break;
            case R.id.rotate_right:
                this.selectedShip.turnShipRight();
                break;
            case R.id.rotate_left:
                this.selectedShip.turnShipLeft();
                break;
        }
        unhighlightCells(oldCells);
        highlightCells(this.selectedShip.getShipsCells());
    }

    public void onClickReady(View view) {
        if (!this.controller.getCurrentGrid().getShipSet().placementLegit()) {
            //TODO: show popup explaining correct ship placement
            return;
        }

        if (    this.controller.getMode() == GameMode.VS_AI_EASY ||
                this.controller.getMode() == GameMode.VS_AI_HARD) {
            //Call GameActivity and provide GameController
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("controller", this.controller);
            startActivity(intent);
        } else if (this.controller.getMode() == GameMode.VS_PLAYER) {
            if (this.controller.getCurrentPlayer()) {
                //Call GameActivity and provide GameController
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("controller", this.controller);
                startActivity(intent);
            } else {
                //TODO: implement transition between players
                this.controller.switchPlayers();
                setupGridView(this.controller.getGridSize());
            }
        }

    }

    private void setupPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
