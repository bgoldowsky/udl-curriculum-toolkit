<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet[
<!ENTITY catts "@id|@class|@title|@xml:lang">
<!ENTITY cncatts "@id|@title|@xml:lang">
 ]>
<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/1999/xhtml" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:wicket="http://wicket.apache.org"
    exclude-result-prefixes="dtb">
    
    <xsl:import href="dtbook2xhtml.xsl"/>
    <xsl:import href="common.xsl"/>    

	<xsl:template match="dtb:responsegroup">
		
		<!-- Determine response type -->
		<xsl:variable name="type">
			<xsl:choose>
				<xsl:when test="self::dtb:responsegroup/dtb:clozepassage">cloze_passage</xsl:when>
				<xsl:when test="self::dtb:responsegroup/dtb:select1/@class='rate'">rate_it</xsl:when>
				<xsl:when test="self::dtb:responsegroup/dtb:select1">multiple_choice</xsl:when>
				<xsl:when test="self::dtb:responsegroup">prompt_response</xsl:when>
				<xsl:otherwise>default</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

    	<xsl:variable name="noAnswer" select="boolean(count(self::dtb:responsegroup/dtb:select1/dtb:item)>0 
 		    									and (count(self::dtb:responsegroup/dtb:select1/dtb:item[@correct='true'])=0)) "/>     
   		
		<div wicket:id="itemSummary_" rgid="{@id}" group="{@group}" type="{$type}" noAnswer="{$noAnswer}">
			<xsl:apply-templates select=".//dtb:item" mode="summary">
				<xsl:with-param name="mode" select="$type" />
			</xsl:apply-templates>
		</div>
	</xsl:template>
	
	<!-- Response prompts -  -->	
   	<xsl:template match="dtb:prompt">
        <xsl:apply-templates/>
     </xsl:template>

	<xsl:template match="dtb:item" mode="summary">
		<xsl:param name="mode" />
    	<xsl:variable name="noAnswer" select="boolean(count(../dtb:item)>0 and (count(../dtb:item[@correct='true'])=0)) "/>     
		<xsl:choose>
			<xsl:when test="$mode = 'rate_it'">
				<!-- Rate-It: copied from UDL Studio but not used here yet. -->
				<div class="rateSel">
					<input id="{@id}" type="radio" disabled="disabled" />
					<br />
					<label for="{@id}">
						<img width="67" height="54" src="img/icons/affect_{@class}.png" >
							<xsl:attribute name="alt">
								A face representing 
								<xsl:apply-templates select="dtb:label" />
							</xsl:attribute>
						</img>
						<br />
						<xsl:apply-templates select="dtb:label" />
					</label>
					<br />
					<div class="countBox">
						<span wicket:id="count_" xmlId="{@id}" class="count">3</span> / <span wicket:id="total_" class="total">9</span>
					</div>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<!-- Multiple Choice -->
        		<div class="responseItem">
					<div class="responseMCItem">
						<input id="{@id}" type="radio" disabled="disabled">
							<xsl:if test="@correct = 'true'">
								<xsl:attribute name="checked">checked</xsl:attribute>
							</xsl:if>
						</input>
						<label for="{@id}">
							<xsl:apply-templates select="dtb:label"/>
							<xsl:choose>
								<!-- only add correct info when there is a correct answer -->
								<xsl:when test="$noAnswer != 'true'">
									<xsl:choose>	
										<xsl:when test="@correct = 'true'">
											<span class="correct resultFeedback"> (Correct)</span>
										</xsl:when>
										<xsl:otherwise>
											<span class="incorrect resultFeedback"> (Incorrect)</span>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
							</xsl:choose>
						</label>
					</div>
					<div>
						<div wicket:id="data_" xmlId="{@id}" correct="{@correct}"></div>
					</div>
					<div class="responseMCFeedback">
						<xsl:apply-templates select="dtb:message" />
					</div>
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="dtb:label">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="dtb:message">
		<div>
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="ancestor::dtb:item/@correct = 'true'">stResult correct</xsl:when>
					<xsl:otherwise>stResult incorrect</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:apply-templates />
		</div>
	</xsl:template>
		
	
</xsl:stylesheet>


   