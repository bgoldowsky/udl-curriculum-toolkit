function cwmImportGrid(tableId, textAreaId, url, readonly) {

    var table = $("#"+tableId); //table for the grid
    var textArea = $("#"+textAreaId); //form text are

    console.log("import the values from the textarea to the table here");
	
    editableGrid = new EditableGrid("DemoGrid"); 
	editableGrid.tableLoaded = function() { this.renderGrid(tableId, "testgrid"); };
	editableGrid.loadJSON(url);
    
    
    }
    
    
function cwmExportGrid(tableId, textAreaId) {

    var table = $("#"+tableId);
    var textArea = $("#"+textAreaId);

    console.log("export the values from the table to the textArea");
    }    