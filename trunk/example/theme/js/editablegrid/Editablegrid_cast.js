var myObject;

function cwmImportGrid(divId, url, readonly) {
    editableGrid = new EditableGrid("DemoGrid");     
	editableGrid.tableLoaded = function() { 
		if(readonly=="true") {
			for ( i=0; i < editableGrid.getColumnCount(); i++) {
				editableGrid.getColumn(i).editable = false;												
			}			
		}

		this.renderGrid(divId, "dataTable"); 
	}; //testgrid is class of table
	editableGrid.loadJSON(url);
	this.initializeGrid();

	//pull text down from URL
	var data = $.ajax({type: "GET", url: url, async: false}).responseText;;

	//parse string into object
	myObject = JSON.parse(data);

}

function cwmExportGrid(textAreaId) {
	jQuery('#'+textAreaId).text(JSON.stringify(myObject));  
}

function initializeGrid() 
{
	with (this.editableGrid) {
		modelChanged = function(rowIndex, columnIndex, oldValue, newValue, row) { 
			myObject.data[rowIndex].values[myObject.metadata[columnIndex].name] = newValue;

		};				
	}
}

