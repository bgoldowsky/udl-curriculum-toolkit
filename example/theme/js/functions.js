/*=========================================================*/
/* this js must be loaded after en.js
/*=========================================================*/



/*=========================================================*/
/* this is called when an js event needs to be logged
/*=========================================================*/
/* TODO is there a way to make this info JS safe?? */
function logJsEvent(detail, page, type) {

	// logJsEventCallbackUrl is a global js var defined in java.  The value is callback URL for the component
	// the event behavior is attached to.
	wicketAjaxGet(logJsEventCallbackUrl + '&eventDetail=' + detail + '&eventPage=' + page + '&eventType=' + type , function() {}, function() {});

}



function collapseBox() {
    $(".collapseBox").each(function() {
        var boxElm = $(this).get(0);
        // Check to see if this box has already been parsed
        if (jQuery.data(boxElm, "ParsedBox") != true) {
            // Not already parsed - add handlers, etc.
            if ($(this).hasClass("open")) {
                $(this).addClass("expOpen");
                $(this).children(".collapseBody").eq(0).show();
            }
            // Add toggle item
            $(this).find('h2, h3, h4, h5, h6').eq(0).addClass('toggleOffset').prepend('<a href="#" onclick="return false;" class="toggle"></a>');
            if ($(this).hasClass("open")) {
                $(this).children().find(".toggle").eq(0).attr("alt", lang['COLLAPSE']).attr("title", lang['COLLAPSE']).addClass("expOpen");
            } else {
                $(this).children().find(".toggle").eq(0).attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
            }

            // Add click binding
            //$(this).children().eq(0).bind("click", function(event) {
            $(this).find('h2, h3, h4, h5, h6').eq(0).bind("click", function(event) {
                toggleChildBox($(this).parents('.collapseBox').eq(0), event);
            });
        }
        // Set parsed flag
        jQuery.data(boxElm, "ParsedBox", true);
    });
}

function toggleChildBox(n, e) {
    $(n).toggleClass("expOpen");
    if ($(n).hasClass("expOpen")) {
        $(n).children(".collapseBody").eq(0).slideDown();
        $(n).children().find(".toggle").eq(0).attr("alt", lang['COLLAPSE']).attr("title", lang['COLLAPSE']).addClass("expOpen");
    } else {
        $(n).children(".collapseBody").eq(0).slideUp();
        $(n).children().find(".toggle").eq(0).attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
    }
    if (e != null) e.stopPropagation();
}

function expandAll(nodeID) {
	expandAllByClass(nodeID, '.collapseBox');
}

function expandAllByClass(nodeID, classType) {
    $(nodeID + " " + classType ).each(function() {
        $(this).addClass("expOpen");
        $(this).children(".collapseBody").eq(0).show();
        $(this).children().find(".toggle").eq(0).attr("alt", lang['COLLAPSE']).attr("title", lang['COLLAPSE']).addClass("expOpen");
    });
}

function collapseAll(nodeID) {
	collapseAllByClass(nodeID, '.collapseBox');
}

function collapseAllByClass(nodeID, classType) {
    $(nodeID + " " + classType).each(function() {
        $(this).removeClass("expOpen");
        $(this).children(".collapseBody").eq(0).hide();
        $(this).children().find(".toggle").eq(0).attr("alt", lang['EXPAND']).attr("title", lang['EXPAND']).removeClass("expOpen");
    });
}

function collapseBoxStatus(id) {
	var collapseBox = $("#" + id).parents(".collapseBox");
	if (collapseBox.length >= 1) { // we're in a collapseBox
		if (collapseBox.eq(0).hasClass("expOpen"))
			return "close";
		else
			return "open";
	}
	return null;
}

/*=========================================================*/
/* Show/Hide student names                                 */
/*=========================================================*/
function showHideNames() {

	/* identify names by finding elements with the responseAuthor class */
	$(".responseAuthor").each(function(index) {
      var nameElm = $(this);

      if (nameElm.css('display') != 'none') {
    	  nameElm.hide();
      }
      else {
    	  nameElm.show();
      }
    });
}

/*=========================================================*/
/* Image Caption Sizing                                    */
/*=========================================================*/
/**
 * Automatically resizes ".imgBox" images and ".objectBox" objects
 * to fit the "img.thumb" or "div.mediaPlaceholder" that is inside.
 * This forces captions to the same width as the content. Image element
 * needs to have the class captionSizer for this to work.
 *
 */
function resizeCaptions(scope) {
    if (scope == null) {
        scope = 'body';
    }
    $(scope).find(".imgBox").each(function() {
        var boxElm = $(this).get(0);
        // Check to see if this box has already been parsed
        if (jQuery.data(boxElm, "ParsedBox") != true) {
            var imgWidth = $('img.captionSizer', this).width();
            // this is required when the img is on the DAV server
            // it makes a cloned image visible off screen to determine the width
            // the image is then removed
            if ( (imgWidth == null) || (imgWidth == 0) ) {
                // Find image dimensions
                var thisImg = $(boxElm).find('img.captionSizer').get(0);
                var thisClone = $(thisImg).clone().addClass('offscreen').appendTo('body');
                imgWidth = $(thisClone).width();
                $(thisClone).remove();
            }
            $(this).width(imgWidth);
            // Set parsed flag
            jQuery.data(boxElm, "ParsedBox", true);
        }
    });

    $(scope).find(".objectBox").each(function() {
            var boxElm = $(this).get(0);
            // Check to see if this box has already been parsed
            if (jQuery.data(boxElm, "ParsedBox") != true) {
                var objectWidth = $('div.mediaPlaceholder', this).css("width");
                if (objectWidth != null) {
                    $(this).width(objectWidth);
                    // Set parsed flag
                    jQuery.data(boxElm, "ParsedBox", true);
                }
            }
    });
}

/*=========================================================*/
/* if you have a thumb image showing, this will show/hide the
 * detailed image and long description
/*=========================================================*/
function showImageDetail(id, show) {

	var image = $("#image_" + id);
	var detail = $("#imageDetail_" + id);

	if (show) {
        // Get dimensions of modal window
        detail.css("position", "absolute").css("left", "-9999px").css("display", "block");
        detailWidth = detail.outerWidth();
        detail.css("left", "auto").css("display", "none");

		// get the position of the original thumbnail and width of browser window
		var thumbPosition = image.position();
		var thumbOffset = image.offset();
		var thumbWidth = image.outerWidth(true);
		var windowWidth = $(window).width();

        // Check to make sure modal won't bleed off right edge
        if ( $(document.body).hasClass("themeGlossary") && (windowWidth < (thumbOffset.left + detailWidth)) ) {
            // Has bleed in glossary window - align to top right of thumbnail
            var glossLeft = (thumbPosition.left + thumbWidth) - detailWidth ;
            detail.css("left",  glossLeft + "px");
            detail.css("right", "auto");
        } else if (windowWidth < (thumbOffset.left + detailWidth)) {
		    // Has bleed - align to top of thumbnail, right side of mainContent
		    mainContent = $("#mainContent");
		    mainContentLeft = $("#mainContentLeft");
		    mainContentWidth = mainContent.outerWidth();
		    mainContentLeftWidth = mainContentLeft.outerWidth();
            var newRight = mainContentLeftWidth - mainContentWidth;
		    detail.css("right", newRight + "px");
		    detail.css("left", "auto");
	    } else {
	        // No bleed - align to top left of thumbnail
		    detail.css("left", thumbPosition.left);
		    detail.css("right", "auto");
		}

		// Store trigger
		var $trigger = $(document.activeElement);
		detail.data("detailTrigger", $trigger.attr("id"));

        detail.css("position", "absolute");
        detail.css("top", thumbPosition.top);
	    detail.attr("tabindex", "-1");
	    detail.get(0).focus();
	    detail.show();
        //image.children('a').hide();
	} else {
        detail.hide();
		//image.children('a').show();
	    if (detail.data("DialogTrigger")) {
			var $trigger = $("#" + detail.data("DialogTrigger"));
			if ($trigger.get(0)) {
				$trigger.get(0).focus();
			}
		}
	}
}

/*=========================================================*/
/* if you have a thumb video image showing, this will show/hide the
 * detailed full video
/*=========================================================*/
function showMediaDetail(id, show) {

	var videoThumb = $("#media_" + id);
	var videoModal = $("#mediaDetail_" + id);
    var videoModalMarginLeft = -videoModal.width() / 2;

	if (show) {
		// get the position of the original thumbnail
		var position = videoThumb.offset();
		videoThumb.children('a').hide();
		videoModal.css("top", position.top);
		videoModal.css("left", "50%");
		videoModal.css("margin-left", videoModalMarginLeft);
		videoModal.css("position", "absolute");
		videoModal.show();

	} else {
		videoModal.hide();
		videoThumb.children('a').show();
		videoThumb.fadeTo(10,1.00);
	}
}

// TODO -- why is this no-op function here?
function closeVideo(id) {
	var videoModal = $("#mediaDetail_" + id);

	// find the child object
	var videoObject = videoModal.children('object');
}


//Fade Out, Swap, Fade In
function fadeOutIn(id) {
	$(id).fadeOut('slow').fadeIn('slow');
}

function modalInit() {
    modalMove();
}

/*=========================================================*/
/* Draggable Modal Handler                                 */
/*=========================================================*/

var moveStepSize = 3;
var moveZIndex = 10;

function modalMove() {
    $(".modalBody").delegate(".modalMove", "mouseover", function() {
        if (!$(this).data("initMouse")) {
            $(this).data("initMouse", true);
            var boxElm = $(this).closest('.modalBody');
            $(boxElm).draggable({
                handle: ".modalMove",
                start: function() { $(boxElm).css('z-index', ++moveZIndex);
                /* logJsEvent("move something", "", "modal:move");*/
                }
            });
        }
    });
    $(".modalBody").delegate(".modalMove", "keydown", function(event) {
        event.stopPropagation();
        var boxElm = $(this).closest('.modalBody');
        switch("" + event.keyCode) {
            case '37': {
                // Left
                moveModalBy(boxElm, -moveStepSize, 0);
                return false;
                break;
            }
            case '38': {
                // Up
                moveModalBy(boxElm, 0, -moveStepSize);
                return false;
                break;
            }
            case '39': {
                // Right
                moveModalBy(boxElm, moveStepSize, 0);
                return false;
                break;
            }
            case '40': {
                // Down
                moveModalBy(boxElm, 0, moveStepSize);
                return false;
                break;
            }
            default: {
                // Do nothing if not understood
                break;
            }
        }
    });
}

function moveModalBy(n, h, v) {
    var newTop = parseInt($(n).css('top')) + v;
    var newLeft = parseInt($(n).css('left')) + h;
    //$(n).offset({ top: newTop, left: newLeft });
    $(n).css('top', newTop + "px");
    $(n).css('left', newLeft + "px");
    $(n).css('z-index', ++moveZIndex);
}

/*=========================================================*/

function thtInit() {
    $('#tht').show();
    thtLocate();
}

/* TextHelp Toolbar Position/Scroll Handling */
function thtLocate() {
    if ( $('#tht').is(":hidden")) { return; }
    if ( !$('#tht').length ) { return; }
    if ( $('#tht').hasClass("thtGlossary") ) { return; }
    if ( $('#tht').hasClass("thtWindow") ) { return; }

    // Get toolbar size
    var thtWidth = parseInt($('#tht').css('width'));
    var thtHeight = parseInt($('#tht').css('height'));

    // Get container position
    var constraint = $("#mainContentLeft").eq(0);
    var topPadding = 0;
    if (!$(constraint).length) {
        var constraint = $("#myContent");
        var topPadding = constraint.innerHeight() - constraint.height(); // padding of myContent
    }

    var pageOffset = constraint.offset();
    var pageHeight = constraint.innerHeight();

    // Get scroll height
    var scrollOffset = $("html").scrollTop();
    if (scrollOffset == 0) {
        // Handle Chrome scroll
        var scrollOffset = $("body").scrollTop();
    }

    if ((pageOffset.top ) >= scrollOffset ) {
        var thtTop = pageOffset.top + topPadding;
        var thtPosition = "absolute";
    } else if ((scrollOffset + thtHeight + topPadding) >= (pageOffset.top + pageHeight)) {
        var thtTop = pageOffset.top + pageHeight - thtHeight - topPadding;
        var thtPosition = "absolute";
    } else {
        //var thtTop = pageOffset.top;
        if ( ($.browser.msie) && (parseInt($.browser.version) <= 6) ) {
            var thtTop = scrollOffset;
            var thtPosition = "absolute";
        } else {
            var thtTop = 0;
            var thtPosition = "fixed";
        }
    }

    // Reposition toolbar
    var thtLeft = pageOffset.left;

    $('#tht').css('top', thtTop + 'px');
    $('#tht').css('left', thtLeft + 'px');
    $('#tht').css('position', thtPosition);
}

/*=========================================================*/

function getSelectedText() {
    if (window.getSelection) {
        return window.getSelection().toString();
    } else if (document.selection && document.selection.createRange) {
        return document.selection.createRange().text;
    }
}

/*=========================================================*/

// Given the IDs of two objects, find the vertical position of the first,
// and move the second object to the matching vertical position.
function matchVerticalPosition(fromId, toId) {
    var offset = $('#'+toId).offset();
    offset.top = $('#'+fromId).offset().top;
    $('#'+toId).offset(offset);
}

/*=========================================================*/

/**
 * This will bind all section links (class='sectionLink') to open their
 * href in the window.opener
 *
 * @return void
 */

function bindSectionOpenerLinks() {
	$('.sectionLink').each(function() {
		if (jQuery.data($(this), "ParsedSectionLink") != true) {
			$(this).bind('click', function(event) {
				window.opener.location = this.href;
				return false;
			});
            // Set parsed flag
            jQuery.data($(this), "ParsedSectionLink", true);
        }
	});
}

/*=========================================================*/
//toggle glossary definition
/*=========================================================*/
function togglespan(elt) {
	var def = $(elt).next("span.inlineGlossary");
	if (def.css('display') != "none")
		def.hide();
	else
		def.show();
	return false;
}

/*=========================================================*/

$(window).ready(function() {
    collapseBox();
    thtInit();
    modalInit();
});

$(window).load(function() {
    resizeCaptions();
    thtLocate();
});

$(window).resize(function() {
    thtLocate();
});

$(window).scroll(function() {
    thtLocate();
});
