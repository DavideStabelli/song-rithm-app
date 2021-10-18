package it.davidestabelli.songrithmapp.Sprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
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
    private Texture deleteButtonOpenTexture;

    public ImportedFileList() {
		super("LISTA DEI BRANI IMPORTATI");

        deleteButtonTexture = new Texture("trash_close.png");
        deleteButtonOpenTexture = new Texture("trash_open.png");
        deleteButton = new VisImage(deleteButtonTexture);
        deleteButton.setSize(20,20);
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
		row();
		add(deleteButton);
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

    public MusicConverter getSelectedMusicFile(){
        Optional optionalValue = Optional.of(tree.getSelectedValue());
        if(!optionalValue.isPresent())
            return null;
        else{            
            return ImportedFileHandler.getMusicFileFromImport((String)optionalValue.get());
        }
    }

	static class ImportedFile extends Tree.Node {
		public ImportedFile (Actor actor) {
			super(actor);
		}
	}
}