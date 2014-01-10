<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:wicket="http://wicket.apache.org"
    xmlns="http://www.w3.org/1999/xhtml" 
    exclude-result-prefixes="dtb">
    
    <!-- This stylesheet will transform things to plain text, for use in emails. -->
    
	<xsl:variable name="newline">
	  <xsl:text>
</xsl:text>
	</xsl:variable>

    <xsl:template match="dtb:level1">
      <html>
        <head>
          <xsl:apply-templates select="dtb:h1"/>
        </head>
        <body>
          <xsl:apply-templates select="dtb:*[local-name()!='h1']"/>
        </body>
      </html>
    </xsl:template>
    
    <!-- h1 is used as subject line of email -->
	<xsl:template match="dtb:h1">
	  <title>
	  	 <xsl:apply-templates/>
	  </title>
	</xsl:template>
	
	<xsl:template match="dtb:p">
	  <xsl:apply-templates/>
	  <xsl:value-of select="concat($newline,$newline)"/>
	</xsl:template>
	
	<xsl:template match="dtb:br">
	  <xsl:value-of select="$newline"/>
	</xsl:template>
	    
    <xsl:template match="dtb:li">
	  <xsl:text>  * </xsl:text>
	  <xsl:apply-templates/>
	  <xsl:value-of select="$newline"/>
	</xsl:template>
	
    <!-- "samp" is used for "variables" that should be replaced -->
    <xsl:template match="dtb:samp">
      <xsl:text>@#@</xsl:text>
        <xsl:apply-templates/>
      <xsl:text>@#@</xsl:text>
    </xsl:template>

	<!-- default, ignore tags but output text. -->
	<xsl:template match="dtb:*">
	  <xsl:apply-templates/>
	</xsl:template>

</xsl:stylesheet>
