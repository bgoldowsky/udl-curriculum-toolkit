function cwmImportGrid(divId, textAreaId, url, readonly) {
	
    editableGrid = new EditableGrid("DemoGrid"); 
	editableGrid.tableLoaded = function() { this.renderGrid(divId, "testgrid"); }; //testgrid is class of table
	editableGrid.loadJSON(url);
    
}