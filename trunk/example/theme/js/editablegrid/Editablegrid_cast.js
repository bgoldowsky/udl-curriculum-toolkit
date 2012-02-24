function cwmImportGrid(divId, textAreaId, url, readonly) {

    editableGrid = new EditableGrid("DemoGrid"); 
	console.log("YEAH here we are");
    
	editableGrid.tableLoaded = function() { this.renderGrid(divId, "testgrid"); }; //testgrid is class of table
	console.log("YEAH here we are 2");
	editableGrid.loadJSON(url);
	console.log("YEAH here we are 3");

}