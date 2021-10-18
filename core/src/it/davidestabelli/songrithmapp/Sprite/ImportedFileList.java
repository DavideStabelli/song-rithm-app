package it.davidestabelli.songrithmapp.Sprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTree;
import com.kotcrab.vis.ui.widget.VisWindow;

import it.davidestabelli.songrithmapp.Helper.ImportedFileHandler;
import it.davidestabelli.songrithmapp.Helper.MusicConverter;

public class ImportedFileList extends VisWindow {

    private VisTree tree;

    public ImportedFileList() {
		super("LISTA DEI BRANI IMPORTATI");

		TableUtils.setSpacingDefaults(this);
		columnDefaults(0).left();

		addAllVoices();
	}

	private void addAllVoices() {
		tree = new VisTree();
        List<ImportedFile> importedFiles = new ArrayList<ImportedFile>();
        for (String fileName : ImportedFileHandler.getFileList()) {
            ImportedFile importedFile = new ImportedFile(new VisLabel(fileName));
            importedFile.setValue(fileName);
            importedFiles.add(importedFile);
            tree.add(importedFile);
        }

		add(tree).expand().fill();
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