// Borrowed from: http://simplyaccessible.com/examples/css-menu/option-3/
// Modified to work on more than 2 levels, and added a hover class to highlight
// parent node when in a sub menu

/*
TODO:
- convert to format similar to new highlighter ?
- add argument capabilities
- add sub menu arrow indicator w/toggle
- add mobile handling
*/

$.fn.CAST_Dropdown_Menu = function() {
    // Check for sub menu items
    $(this).find('ul').each(function() {
        $(this).closest('li').addClass('show-sub').find('a').eq(0).append(' <span class="sub-mark">&gt;</span>');
    });

    // Set tabIndex to -1 so that links can't receive focus until menu is open
    $(this).find('li > a').next('ul').find('a').attr('tabIndex',-1);

    $(this).find('li > a').hover(function(){
        $(this).closest('ul').find('.show-menu').removeClass('show-menu').find('a').attr('tabIndex',-1);
    });
    $(this).find('li > a').focus(function(){
        $(this).closest('ul').find('.show-menu').removeClass('show-menu').find('a').attr('tabIndex',-1);
        $(this).next('ul').addClass('show-menu').find('a').attr('tabIndex',0);

        $(this).closest('ul').find('.show-hover').removeClass('show-hover');
        $(this).closest('ul').find('.show-menu').closest('li').addClass('show-hover');
    });

    // Hide menu if click or focus occurs outside of navigation
    $(this).find('a').last().keydown(function(e){
        if(e.keyCode == 9) {
            // If the user tabs out of the navigation hide all menus
            $('.show-menu').removeClass('show-menu').find('a').attr('tabIndex',-1);
        }
    });
    $(document).click(function(){ $('.show-menu').removeClass('show-menu').find('a').attr('tabIndex',-1); });

    $(this).click(function(e){
        e.stopPropagation();
    });
}