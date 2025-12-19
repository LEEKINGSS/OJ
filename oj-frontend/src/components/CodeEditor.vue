<template>
  <div ref="editorContainer" style="width: 100%; height: 100%"></div>
</template>
<script setup lang="ts">
// 导入monaco编辑器
import * as monaco from "monaco-editor";
import { ref, onMounted, onUnmounted, toRaw, defineProps, watch } from "vue";
//定义父组件传来的属性
const props = defineProps({
  codeValue: {
    type: String,
    default: "// 这里是初始代码",
  },
  language: {
    type: String,
    default: "java",
  },
  codeChange: {
    type: Function,
    default: (value: string) => {
      console.log(value);
    },
  },
});

// 编辑器容器div
const editorContainer = ref(null);
// 编辑器实列
const editor = ref();
// 初始化编辑器
// 更新编译器
watch(
  () => props.language, // 监听的目标：props.language
  () => {
    monaco.editor.setModelLanguage(
      // 设置 Monaco 编辑器的语言
      // 踩坑一定要使用toRaw
      toRaw(editor.value).getModel(), // 获取当前编辑器的 model 并转换为原始对象
      props.language // 使用新的 language 设置编辑器的语言
    );
    console.log(
      "当前语言为：",
      props.language,
      "当前语言为：",
      toRaw(editor.value).getModel().getLanguageId()
    );
  }
);
watch(
  () => props.codeValue,
  (newVal) => {
    const currentValue = toRaw(editor.value).getValue();
    if (newVal !== currentValue) {
      toRaw(editor.value).setValue(newVal);
    }
  }
);

onMounted(() => {
  if (editorContainer.value) {
    // 创建编辑器实列
    editor.value = monaco.editor.create(editorContainer.value, {
      value: props.codeValue, // 编辑器初始内容
      language: "java", // 编辑器语言
      theme: "vs-dark", // 编辑器主题 默认vs-dark VSCode 黑色主题,可选：vs、vs-dark、hc-black、hc-light
      readOnly: false, // 是否只读内容不可编辑
      readOnlyMessage: {
        value: "不可以修改哦",
        supportThemeIcons: true,
        supportHtml: true,
      }, // 为只读时编辑内日提示词
      codeLens: true, // 代码透镜
      folding: true, // 代码折叠
      snippetSuggestions: "inline", // 代码提示
      tabCompletion: "on", // 代码提示按tab完成
      foldingStrategy: "auto", // 折叠策略
      smoothScrolling: true, // 滚动动画
      // inDiffEditor: 是否在差异编辑器中使用
      // ariaLabel: 编辑器文本区域的无障碍标签
      // ariaRequired: 是否设置 aria-required 属性
      // screenReaderAnnounceInlineSuggestion: 是否立即宣布内联建议内容
      // tabIndex: 编辑器文本区域的 tabindex 属性
      // rulers: 指定位置渲染垂直线
      // wordSegmenterLocales: 用于词分割的语言
      // wordSeparators: 词导航时使用的分隔符
      // selectionClipboard: 启用 Linux 主剪贴板
      // lineNumbers: 控制行号的显示方式
      // cursorSurroundingLines: 控制光标周围的可见行数
      // cursorSurroundingLinesStyle: 控制何时强制执行 cursorSurroundingLines
      // renderFinalNewline: 渲染文件末尾的新行
      // unusualLineTerminators: 移除不寻常的行终止符
      // selectOnLineNumbers: 单击行号时选择相应行
      // lineNumbersMinChars: 控制行号的最小字符数
      // glyphMargin: 启用渲染图符边距
      // lineDecorationsWidth: 行装饰的宽度
      // revealHorizontalRightPadding: 水平方向显示光标时的虚拟填充
      // roundedSelection: 选择是否带有圆角
      // extraEditorClassName: 添加到编辑器的类名
      // readOnly: 编辑器是否只读
      // readOnlyMessage: 只读模式下的提示信息
      // domReadOnly: 输入文本框是否使用 DOM 的 readonly 属性
      // linkedEditing: 启用关联编辑
      // renderValidationDecorations: 是否渲染验证装饰
      // scrollbar: 控制滚动条的行为和渲染
      // stickyScroll: 控制粘性滚动选项
      // minimap: 控制最小地图的行为和渲染
      // find: 控制查找小部件的行为
      // fixedOverflowWidgets: 以 fixed 方式显示溢出小部件
      // overviewRulerLanes: 概览尺子应渲染的车道数
      // overviewRulerBorder: 是否绘制概览尺子边框
      // cursorBlinking: 光标闪烁风格
      // mouseWheelZoom: 使用鼠标滚轮结合 Ctrl 键缩放字体
      // mouseStyle: 鼠标指针样式
      // cursorSmoothCaretAnimation: 平滑光标动画
      // cursorStyle: 光标样式
      // cursorWidth: 光标宽度
      // fontLigatures: 启用字体连字
      // fontVariations: 启用字体变体
      // defaultColorDecorators: 是否使用默认颜色装饰
      // disableLayerHinting: 禁用编辑器层提示
      // disableMonospaceOptimizations: 禁用等宽字体优化
      // hideCursorInOverviewRuler: 是否在概览尺子中隐藏光标
      // scrollBeyondLastLine: 启用滚动到最后行
      // scrollBeyondLastColumn: 启用滚动到最后列
      // smoothScrolling: 启用平滑滚动
      // automaticLayout: 启用自动布局
      // wordWrap: 控制自动换行
      // wordWrapOverride1: 覆盖 wordWrap 设置
      // wordWrapOverride2: 覆盖 wordWrapOverride1 设置
      // wordWrapColumn: 换行列数
      // wrappingIndent: 控制换行的缩进
      // wrappingStrategy: 控制换行策略
      // wordWrapBreakBeforeCharacters: 换行前的字符
      // wordWrapBreakAfterCharacters: 换行后的字符
      // wordBreak: 控制单词换行
      // stopRenderingLineAfter: 停止渲染行的位置
      // hover: 配置编辑器的悬浮提示
      // links: 启用链接检测
      // colorDecorators: 启用内联颜色装饰
      // colorDecoratorsActivatedOn: 触发颜色选择器的条件
      // colorDecoratorsLimit: 内联颜色装饰的最大数量
      // comments: 控制编辑器中的注释行为
      // contextmenu: 启用自定义上下文菜单
      // mouseWheelScrollSensitivity: 鼠标滚轮滚动灵敏度
      // fastScrollSensitivity: 快速滚动速度
      // scrollPredominantAxis: 启用主要轴滚动
      // columnSelection: 启用列选择
      // multiCursorModifier: 添加多个光标的修饰键
      // multiCursorMergeOverlapping: 合并重叠的选择
      // multiCursorPaste: 配置粘贴多个文本时的行为
      // multiCursorLimit: 控制同时可以存在的最大文本光标数量
      // accessibilitySupport: 配置编辑器的无障碍支持
      // accessibilityPageSize: 屏幕阅读器可读取的行数
      // suggest: 建议选项
      // inlineSuggest: 内联建议选项
      // experimentalInlineEdit: 实验性内联编辑选项
      // smartSelect: 智能选择选项
      // gotoLocation: 跳转到位置选项
      // quickSuggestions: 启用快速建议
      // quickSuggestionsDelay: 快速建议显示延迟
      // padding: 控制编辑器的边距
      // parameterHints: 参数提示选项
      // autoClosingBrackets: 自动闭合括号的选项
      // autoClosingComments: 自动闭合注释的选项
      // autoClosingQuotes: 自动闭合引号的选项
      // autoClosingDelete: 自动闭合删除的选项
      // autoClosingOvertype: 自动闭合覆盖的选项
      // autoSurround: 自动环绕的选项
      // autoIndent: 控制自动缩进
      // stickyTabStops: 插入和删除空白时遵循制表符停止
      // formatOnType: 启用输入时格式化
      // formatOnPaste: 启用粘贴时格式化
      // dragAndDrop: 允许通过拖放移动选择
      // suggestOnTriggerCharacters: 在触发字符上弹出建议框
      // acceptSuggestionOnEnter: 在 Enter 键上接受建议的方式
      // acceptSuggestionOnCommitCharacter: 在提供者定义的字符上接受建议
      // snippetSuggestions: 启用片段建议
      // emptySelectionClipboard: 无选择时复制当前行
      // copyWithSyntaxHighlighting: 复制时包含语法高亮
      // suggestSelection: 建议的历史模式
      // suggestFontSize: 建议小部件的字体大小
      // suggestLineHeight: 建议小部件的行高
      // tabCompletion: 启用 Tab 完成
      // selectionHighlight: 启用选择高亮
      // occurrencesHighlight: 启用语义发生高亮
      // codeLens: 显示代码透镜
      // codeLensFontFamily: 代码透镜的字体系列
      // codeLensFontSize: 代码透镜的字体大小
      // lightbulb: 控制代码操作灯泡的行为
      // codeActionsOnSaveTimeout: 保存时运行代码操作的超时时间
      // folding: 启用代码折叠
      // foldingStrategy: 选择折叠策略
      // foldingHighlight: 启用折叠区域高亮
      // foldingImportsByDefault: 默认折叠导入区域
      // foldingMaximumRegions: 最大可折叠区域数
      // showFoldingControls: 折叠控制的显示方式
      // unfoldOnClickAfterEndOfLine: 单击折行后展开
      // matchBrackets: 启用匹配括号高亮
      // experimentalWhitespaceRendering: 实验性空白渲染
      // renderWhitespace: 渲染空白字符
      // renderControlCharacters: 渲染控制字符
      // renderLineHighlight: 渲染当前行高亮
      // renderLineHighlightOnlyWhenFocus: 仅当编辑器聚焦时渲染当前行高亮
      // useTabStops: 插入和删除空白时遵循制表符停止
      // fontFamily: 字体系列
      // fontWeight: 字体粗细
      // fontSize: 字体大小
      // lineHeight: 行高
      // letterSpacing: 字母间距
      // showUnused: 控制未使用变量的高亮
      // peekWidgetDefaultFocus: 默认聚焦预览窗口的小部件
      // placeholder: 编辑器为空时显示的占位符
      // definitionLinkOpensInPeek: 定义链接是否在预览窗口中打开
      // showDeprecated: 控制废弃变量的高亮
      // matchOnWordStartOnly: 是否允许单词中间匹配
      // inlayHints: 控制内联提示的行为和渲染
    });

    // 编辑器内容变更时回调
    editor.value.onDidChangeModelContent(() => {
      const value = toRaw(editor.value).getValue();
      props.codeChange(value);
    });
  }
});

// // 当组件被卸载时调用该回调函数
// onUnmounted(() => {
//   // 检查 editor 是否已初始化
//   if (editor.value) {
//     // 清理 editor 资源，避免内存泄漏
//     editor.value.dispose();
//   }
// });
</script>
