package it.davidestabelli.songrithmapp.Sprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;

import it.davidestabelli.songrithmapp.Helper.ImportedFileHandler;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;

public class ImportedFileList extends VisWindow {

    private VisTree tree;
    private VisImage deleteButton;

    private Texture deleteButtonTexture;

    public ImportedFileList(Stage parentStage) {
		super("LISTA DEI BRANI IMPORTATI");

        deleteButtonTexture = new Texture("trash_close.png");
        deleteButton = new VisImage(deleteButtonTexture);
        deleteButton.setSize(35, 35);
        deleteButton.toFront();
        deleteButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if(tree.getSelectedValue() != null) {
                    ImportedFileHandler.deleteImport((String) tree.getSelectedValue());
                    updateVoices();
                }
            }
        });

		TableUtils.setSpacingDefaults(this);
		columnDefaults(0).left();

		addAllVoices();
        parentStage.addActor(this);
        parentStage.addActor(deleteButton);
	}

	private void addAllVoices() {
		tree = new VisTree();
        for (String fileName : ImportedFileHandler.getFileList()) {
            ImportedFile importedFile = new ImportedFile(new VisLabel(fileName));
            importedFile.setValue(fileName);
            tree.add(importedFile);
        }

        add(tree).expand().fill();
	}

    public void updateVoices() {
        tree.clearChildren();
        for (String fileName : ImportedFileHandler.getFileList()) {
            ImportedFile importedFile = new ImportedFile(new VisLabel(fileName));
            importedFile.setValue(fileName);
            tree.add(importedFile);
        }
    }

    public void update(){
        Tree.Node selectedNode = tree.getSelectedNode();
        if(selectedNode != null){
            deleteButton.setVisible(true);
            float position = selectedNode.getActor().getY() - (deleteButton.getHeight() - selectedNode.getActor().getHeight()) / 2;
            deleteButton.setY(position);
            deleteButton.setX(tree.getX() + tree.getWidth());
        } else {
            deleteButton.setVisible(false);
        }
    }

    public MusicConverter getSelectedMusicFile(){
        Object optionalValue = tree.getSelectedValue();
        if(optionalValue == null)
            return null;
        else{            
            return ImportedFileHandler.getMusicFileFromImport((String)optionalValue);
        }
    }

	static class ImportedFile extends Tree.Node {
		public ImportedFile (Actor actor) {
			super(actor);
		}
	}
}