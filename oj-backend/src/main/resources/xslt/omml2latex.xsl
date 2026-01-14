<?xml version="1.0" encoding="UTF-8"?>
<!--
  OMML (Office Math Markup Language) to LaTeX Converter
  Simplified version for common math expressions
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    exclude-result-prefixes="m w">

  <xsl:output method="text" encoding="UTF-8"/>

  <!-- Root template -->
  <xsl:template match="/">
    <xsl:apply-templates select="//m:oMath|//m:oMathPara"/>
  </xsl:template>

  <!-- Math paragraph (display mode) -->
  <xsl:template match="m:oMathPara">
    <xsl:apply-templates select="m:oMath"/>
  </xsl:template>

  <!-- Main math element -->
  <xsl:template match="m:oMath">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Run (text content) -->
  <xsl:template match="m:r">
    <xsl:apply-templates select="m:t"/>
  </xsl:template>

  <!-- Text -->
  <xsl:template match="m:t">
    <xsl:call-template name="replace-special-chars">
      <xsl:with-param name="text" select="."/>
    </xsl:call-template>
  </xsl:template>

  <!-- Fraction -->
  <xsl:template match="m:f">
    <xsl:text>\frac{</xsl:text>
    <xsl:apply-templates select="m:num"/>
    <xsl:text>}{</xsl:text>
    <xsl:apply-templates select="m:den"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="m:num|m:den">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Superscript -->
  <xsl:template match="m:sSup">
    <xsl:apply-templates select="m:e"/>
    <xsl:text>^{</xsl:text>
    <xsl:apply-templates select="m:sup"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Subscript -->
  <xsl:template match="m:sSub">
    <xsl:apply-templates select="m:e"/>
    <xsl:text>_{</xsl:text>
    <xsl:apply-templates select="m:sub"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Superscript and Subscript -->
  <xsl:template match="m:sSubSup">
    <xsl:apply-templates select="m:e"/>
    <xsl:text>_{</xsl:text>
    <xsl:apply-templates select="m:sub"/>
    <xsl:text>}^{</xsl:text>
    <xsl:apply-templates select="m:sup"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="m:e|m:sup|m:sub">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Radical (square root) -->
  <xsl:template match="m:rad">
    <xsl:choose>
      <xsl:when test="m:deg and normalize-space(m:deg) != ''">
        <xsl:text>\sqrt[</xsl:text>
        <xsl:apply-templates select="m:deg"/>
        <xsl:text>]{</xsl:text>
        <xsl:apply-templates select="m:e"/>
        <xsl:text>}</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>\sqrt{</xsl:text>
        <xsl:apply-templates select="m:e"/>
        <xsl:text>}</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="m:deg">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- N-ary operators (sum, product, integral, etc.) -->
  <xsl:template match="m:nary">
    <xsl:variable name="chr" select="m:naryPr/m:chr/@m:val"/>
    <xsl:choose>
      <xsl:when test="$chr = '∑' or contains($chr, 'sum')">
        <xsl:text>\sum</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '∏' or contains($chr, 'prod')">
        <xsl:text>\prod</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '∫' or contains($chr, 'int')">
        <xsl:text>\int</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '∮'">
        <xsl:text>\oint</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '⋃'">
        <xsl:text>\bigcup</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '⋂'">
        <xsl:text>\bigcap</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>\sum</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="m:sub and normalize-space(m:sub) != ''">
      <xsl:text>_{</xsl:text>
      <xsl:apply-templates select="m:sub"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
    <xsl:if test="m:sup and normalize-space(m:sup) != ''">
      <xsl:text>^{</xsl:text>
      <xsl:apply-templates select="m:sup"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="m:e"/>
  </xsl:template>

  <!-- Delimiter (parentheses, brackets, etc.) -->
  <xsl:template match="m:d">
    <xsl:variable name="begChr" select="m:dPr/m:begChr/@m:val"/>
    <xsl:variable name="endChr" select="m:dPr/m:endChr/@m:val"/>
    <xsl:choose>
      <xsl:when test="$begChr = '(' or not($begChr)">
        <xsl:text>\left(</xsl:text>
      </xsl:when>
      <xsl:when test="$begChr = '['">
        <xsl:text>\left[</xsl:text>
      </xsl:when>
      <xsl:when test="$begChr = '{'">
        <xsl:text>\left\{</xsl:text>
      </xsl:when>
      <xsl:when test="$begChr = '|'">
        <xsl:text>\left|</xsl:text>
      </xsl:when>
      <xsl:when test="$begChr = '⌊'">
        <xsl:text>\left\lfloor </xsl:text>
      </xsl:when>
      <xsl:when test="$begChr = '⌈'">
        <xsl:text>\left\lceil </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>\left(</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="m:e"/>
    <xsl:choose>
      <xsl:when test="$endChr = ')' or not($endChr)">
        <xsl:text>\right)</xsl:text>
      </xsl:when>
      <xsl:when test="$endChr = ']'">
        <xsl:text>\right]</xsl:text>
      </xsl:when>
      <xsl:when test="$endChr = '}'">
        <xsl:text>\right\}</xsl:text>
      </xsl:when>
      <xsl:when test="$endChr = '|'">
        <xsl:text>\right|</xsl:text>
      </xsl:when>
      <xsl:when test="$endChr = '⌋'">
        <xsl:text>\right\rfloor </xsl:text>
      </xsl:when>
      <xsl:when test="$endChr = '⌉'">
        <xsl:text>\right\rceil </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>\right)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Matrix -->
  <xsl:template match="m:m">
    <xsl:text>\begin{matrix}</xsl:text>
    <xsl:for-each select="m:mr">
      <xsl:for-each select="m:e">
        <xsl:apply-templates/>
        <xsl:if test="position() != last()">
          <xsl:text> &amp; </xsl:text>
        </xsl:if>
      </xsl:for-each>
      <xsl:if test="position() != last()">
        <xsl:text> \\ </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>\end{matrix}</xsl:text>
  </xsl:template>

  <!-- Bar/Overline -->
  <xsl:template match="m:bar">
    <xsl:text>\overline{</xsl:text>
    <xsl:apply-templates select="m:e"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Accent (hat, tilde, etc.) -->
  <xsl:template match="m:acc">
    <xsl:variable name="chr" select="m:accPr/m:chr/@m:val"/>
    <xsl:choose>
      <xsl:when test="$chr = '̂' or $chr = '^'">
        <xsl:text>\hat{</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '̃' or $chr = '~'">
        <xsl:text>\tilde{</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '̇' or $chr = '.'">
        <xsl:text>\dot{</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '̈'">
        <xsl:text>\ddot{</xsl:text>
      </xsl:when>
      <xsl:when test="$chr = '→'">
        <xsl:text>\vec{</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>\hat{</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="m:e"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Limit -->
  <xsl:template match="m:limLow">
    <xsl:apply-templates select="m:e"/>
    <xsl:text>_{</xsl:text>
    <xsl:apply-templates select="m:lim"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="m:limUpp">
    <xsl:apply-templates select="m:e"/>
    <xsl:text>^{</xsl:text>
    <xsl:apply-templates select="m:lim"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="m:lim">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Function -->
  <xsl:template match="m:func">
    <xsl:apply-templates select="m:fName"/>
    <xsl:apply-templates select="m:e"/>
  </xsl:template>

  <xsl:template match="m:fName">
    <xsl:variable name="name" select="normalize-space(.)"/>
    <xsl:choose>
      <xsl:when test="$name = 'sin'"><xsl:text>\sin </xsl:text></xsl:when>
      <xsl:when test="$name = 'cos'"><xsl:text>\cos </xsl:text></xsl:when>
      <xsl:when test="$name = 'tan'"><xsl:text>\tan </xsl:text></xsl:when>
      <xsl:when test="$name = 'cot'"><xsl:text>\cot </xsl:text></xsl:when>
      <xsl:when test="$name = 'sec'"><xsl:text>\sec </xsl:text></xsl:when>
      <xsl:when test="$name = 'csc'"><xsl:text>\csc </xsl:text></xsl:when>
      <xsl:when test="$name = 'log'"><xsl:text>\log </xsl:text></xsl:when>
      <xsl:when test="$name = 'ln'"><xsl:text>\ln </xsl:text></xsl:when>
      <xsl:when test="$name = 'exp'"><xsl:text>\exp </xsl:text></xsl:when>
      <xsl:when test="$name = 'lim'"><xsl:text>\lim </xsl:text></xsl:when>
      <xsl:when test="$name = 'max'"><xsl:text>\max </xsl:text></xsl:when>
      <xsl:when test="$name = 'min'"><xsl:text>\min </xsl:text></xsl:when>
      <xsl:when test="$name = 'mod'"><xsl:text>\mod </xsl:text></xsl:when>
      <xsl:when test="$name = 'gcd'"><xsl:text>\gcd </xsl:text></xsl:when>
      <xsl:otherwise>
        <xsl:text>\mathrm{</xsl:text>
        <xsl:value-of select="$name"/>
        <xsl:text>}</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Equation array -->
  <xsl:template match="m:eqArr">
    <xsl:text>\begin{aligned}</xsl:text>
    <xsl:for-each select="m:e">
      <xsl:apply-templates/>
      <xsl:if test="position() != last()">
        <xsl:text> \\ </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>\end{aligned}</xsl:text>
  </xsl:template>

  <!-- Box -->
  <xsl:template match="m:box|m:borderBox">
    <xsl:text>\boxed{</xsl:text>
    <xsl:apply-templates select="m:e"/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <!-- Group character -->
  <xsl:template match="m:groupChr">
    <xsl:variable name="chr" select="m:groupChrPr/m:chr/@m:val"/>
    <xsl:variable name="pos" select="m:groupChrPr/m:pos/@m:val"/>
    <xsl:choose>
      <xsl:when test="$pos = 'top'">
        <xsl:text>\overbrace{</xsl:text>
        <xsl:apply-templates select="m:e"/>
        <xsl:text>}</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>\underbrace{</xsl:text>
        <xsl:apply-templates select="m:e"/>
        <xsl:text>}</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Pre-superscript/subscript -->
  <xsl:template match="m:sPre">
    <xsl:text>{}_{</xsl:text>
    <xsl:apply-templates select="m:sub"/>
    <xsl:text>}^{</xsl:text>
    <xsl:apply-templates select="m:sup"/>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates select="m:e"/>
  </xsl:template>

  <!-- Replace special characters with LaTeX equivalents -->
  <xsl:template name="replace-special-chars">
    <xsl:param name="text"/>
    <xsl:variable name="result">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$text"/>
        <xsl:with-param name="from">×</xsl:with-param>
        <xsl:with-param name="to">\times </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result2">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result"/>
        <xsl:with-param name="from">÷</xsl:with-param>
        <xsl:with-param name="to">\div </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result3">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result2"/>
        <xsl:with-param name="from">±</xsl:with-param>
        <xsl:with-param name="to">\pm </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result4">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result3"/>
        <xsl:with-param name="from">≠</xsl:with-param>
        <xsl:with-param name="to">\neq </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result5">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result4"/>
        <xsl:with-param name="from">≤</xsl:with-param>
        <xsl:with-param name="to">\leq </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result6">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result5"/>
        <xsl:with-param name="from">≥</xsl:with-param>
        <xsl:with-param name="to">\geq </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result7">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result6"/>
        <xsl:with-param name="from">∞</xsl:with-param>
        <xsl:with-param name="to">\infty </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result8">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result7"/>
        <xsl:with-param name="from">α</xsl:with-param>
        <xsl:with-param name="to">\alpha </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result9">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result8"/>
        <xsl:with-param name="from">β</xsl:with-param>
        <xsl:with-param name="to">\beta </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result10">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result9"/>
        <xsl:with-param name="from">γ</xsl:with-param>
        <xsl:with-param name="to">\gamma </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result11">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result10"/>
        <xsl:with-param name="from">δ</xsl:with-param>
        <xsl:with-param name="to">\delta </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result12">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result11"/>
        <xsl:with-param name="from">φ</xsl:with-param>
        <xsl:with-param name="to">\varphi </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result13">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result12"/>
        <xsl:with-param name="from">π</xsl:with-param>
        <xsl:with-param name="to">\pi </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result14">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result13"/>
        <xsl:with-param name="from">σ</xsl:with-param>
        <xsl:with-param name="to">\sigma </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result15">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result14"/>
        <xsl:with-param name="from">θ</xsl:with-param>
        <xsl:with-param name="to">\theta </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result16">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result15"/>
        <xsl:with-param name="from">λ</xsl:with-param>
        <xsl:with-param name="to">\lambda </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result17">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result16"/>
        <xsl:with-param name="from">μ</xsl:with-param>
        <xsl:with-param name="to">\mu </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result18">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result17"/>
        <xsl:with-param name="from">ε</xsl:with-param>
        <xsl:with-param name="to">\varepsilon </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result19">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result18"/>
        <xsl:with-param name="from">ω</xsl:with-param>
        <xsl:with-param name="to">\omega </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result20">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result19"/>
        <xsl:with-param name="from">∈</xsl:with-param>
        <xsl:with-param name="to">\in </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result21">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result20"/>
        <xsl:with-param name="from">∉</xsl:with-param>
        <xsl:with-param name="to">\notin </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result22">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result21"/>
        <xsl:with-param name="from">⊂</xsl:with-param>
        <xsl:with-param name="to">\subset </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result23">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result22"/>
        <xsl:with-param name="from">⊆</xsl:with-param>
        <xsl:with-param name="to">\subseteq </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result24">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result23"/>
        <xsl:with-param name="from">∪</xsl:with-param>
        <xsl:with-param name="to">\cup </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result25">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result24"/>
        <xsl:with-param name="from">∩</xsl:with-param>
        <xsl:with-param name="to">\cap </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result26">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result25"/>
        <xsl:with-param name="from">→</xsl:with-param>
        <xsl:with-param name="to">\rightarrow </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result27">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result26"/>
        <xsl:with-param name="from">←</xsl:with-param>
        <xsl:with-param name="to">\leftarrow </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result28">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result27"/>
        <xsl:with-param name="from">⇒</xsl:with-param>
        <xsl:with-param name="to">\Rightarrow </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result29">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result28"/>
        <xsl:with-param name="from">⇔</xsl:with-param>
        <xsl:with-param name="to">\Leftrightarrow </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result30">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result29"/>
        <xsl:with-param name="from">∀</xsl:with-param>
        <xsl:with-param name="to">\forall </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result31">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result30"/>
        <xsl:with-param name="from">∃</xsl:with-param>
        <xsl:with-param name="to">\exists </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result32">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result31"/>
        <xsl:with-param name="from">≡</xsl:with-param>
        <xsl:with-param name="to">\equiv </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result33">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result32"/>
        <xsl:with-param name="from">≈</xsl:with-param>
        <xsl:with-param name="to">\approx </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result34">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result33"/>
        <xsl:with-param name="from">∂</xsl:with-param>
        <xsl:with-param name="to">\partial </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result35">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result34"/>
        <xsl:with-param name="from">∇</xsl:with-param>
        <xsl:with-param name="to">\nabla </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result36">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result35"/>
        <xsl:with-param name="from">⊕</xsl:with-param>
        <xsl:with-param name="to">\oplus </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="result37">
      <xsl:call-template name="string-replace">
        <xsl:with-param name="string" select="$result36"/>
        <xsl:with-param name="from">⊗</xsl:with-param>
        <xsl:with-param name="to">\otimes </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="$result37"/>
  </xsl:template>

  <!-- String replace helper -->
  <xsl:template name="string-replace">
    <xsl:param name="string"/>
    <xsl:param name="from"/>
    <xsl:param name="to"/>
    <xsl:choose>
      <xsl:when test="contains($string, $from)">
        <xsl:value-of select="substring-before($string, $from)"/>
        <xsl:value-of select="$to"/>
        <xsl:call-template name="string-replace">
          <xsl:with-param name="string" select="substring-after($string, $from)"/>
          <xsl:with-param name="from" select="$from"/>
          <xsl:with-param name="to" select="$to"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Ignore unknown elements but process their children -->
  <xsl:template match="*">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>

