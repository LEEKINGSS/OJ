<template>
  <div id="TagsInput">
    <a-input-tag
      placeholder="请输入题目标签"
      :model-value="localTags.map((tag) => tag.value)"
      allow-clear
      @click="visible = true"
      style="width: 100%"
    />
  </div>
  <a-modal
    v-model:visible="visible"
    @ok="handleOk"
    :closable="false"
    :maskClosable="false"
    :escToClose="false"
    :hide-cancel="true"
    width="768px"
  >
    <template #title> 请选择标签 </template>
    <div>
      <a-scrollbar style="height: 600px; width: 728px; overflow-x: auto">
        <div>
          <h3 style="margin: 20px">已选择的</h3>
          <div class="tag-list">
            <a-tag
              v-for="tag in props.tagsChose"
              :key="tag.value"
              class="tag"
              color="arcoblue"
              closable
              :default-checked="true"
              style="margin-left: 10px; margin-bottom: 5px"
              size="large"
              @close="(ev) => customClose(ev, tag)"
              >{{ tag.value }}</a-tag
            >
          </div>
        </div>
        <div v-for="(item, index) in tagsList" :key="index" class="tagBlock">
          <h3 style="margin: 20px">{{ item.title }}</h3>
          <div class="tag-list">
            <a-tag
              v-for="(tag, idx) in item.tags"
              :key="idx"
              class="tag"
              color="red"
              checkable
              :checked="tag.checked"
              style="margin-left: 10px; margin-bottom: 5px"
              size="large"
              @check="(checked, ev) => customCheck(tag, checked, ev)"
              >{{ tag.value }}</a-tag
            >
          </div>
          <a-divider />
        </div>
      </a-scrollbar>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { AutoComplete } from "@arco-design/web-vue";
import { ref, defineProps, watch } from "vue";
/**
 * @description： 标签对话框显示与隐藏
 */
const visible = ref(false);
const handleOk = () => {
  visible.value = false; // 只有点确定才关闭
  props.handSubmit();
};
/**
 * @description： 标签列表
 */
const tagsList = ref([
  {
    title: "语言入门（请选择[入门与面试]题库）",
    tags: [
      { value: "顺序结构", checked: false },
      { value: "分支结构", checked: false },
      { value: "循环结构", checked: false },
      { value: "数组", checked: false },
      { value: "字符串（入门）", checked: false },
      { value: "结构体", checked: false },
      { value: "函数与递归", checked: false },
    ],
  },
  {
    title: "字符串",
    tags: [
      { value: "后缀自动机", checked: false },
      { value: "SAM", checked: false },
      { value: "字典树", checked: false },
      { value: "Trie", checked: false },
      { value: "AC 自动机", checked: false },
      { value: "KMP 算法", checked: false },
      { value: "后缀数组", checked: false },
      { value: "SA", checked: false },
      { value: "后缀树", checked: false },
      { value: "有限状态自动机", checked: false },
      { value: "回文自动机", checked: false },
      { value: "PAM", checked: false },
      { value: "Manacher 算法", checked: false },
      { value: "Lyndon 分解", checked: false },
      { value: "Z 函数", checked: false },
      { value: "后缀平衡树", checked: false },
    ],
  },
  {
    title: "动态规划 DP",
    tags: [
      { value: "背包 DP", checked: false },
      { value: "数位 DP", checked: false },
      { value: "区间 DP", checked: false },
      { value: "树形 DP", checked: false },
      { value: "轮廓线 DP", checked: false },
      { value: "线性 DP", checked: false },
      { value: "状压 DP", checked: false },
    ],
  },
  {
    title: "搜索",
    tags: [
      { value: "广度优先搜索 BFS", checked: false },
      { value: "深度优先搜索 DFS", checked: false },
      { value: "剪枝", checked: false },
      { value: "记忆化搜索", checked: false },
      { value: "启发式搜索", checked: false },
      { value: "迭代加深搜索", checked: false },
      { value: "启发式迭代加深搜索 IDA*", checked: false },
      { value: "Dancing Links", checked: false },
      { value: "爬山算法 Local search", checked: false },
      { value: "模拟退火", checked: false },
      { value: "随机调整", checked: false },
      { value: "遗传算法", checked: false },
      { value: "A* 算法", checked: false },
      { value: "折半搜索 meet in the middle", checked: false },
      { value: "梯度下降法", checked: false },
    ],
  },
  {
    title: "数学",
    tags: [
      { value: "信息论", checked: false },
      { value: "拉格朗日乘数法", checked: false },
      { value: "拉格朗日插值法", checked: false },
      { value: "单位根反演", checked: false },
    ],
  },
  {
    title: "图论",
    tags: [
      { value: "Kruskal", checked: false },
      { value: "重构树", checked: false },
      { value: "网络流", checked: false },
      { value: "图论建模", checked: false },
      { value: "图遍历", checked: false },
      { value: "拓扑排序", checked: false },
      { value: "最短路", checked: false },
      { value: "生成树", checked: false },
      { value: "平面图", checked: false },
      { value: "最小环", checked: false },
      { value: "负权环", checked: false },
      { value: "连通块", checked: false },
      { value: "2-SAT", checked: false },
      { value: "平面图", checked: false },
      { value: "欧拉公式", checked: false },
      { value: "强连通分量 Tarjan", checked: false },
      { value: "双连通分量", checked: false },
      { value: "欧拉回路", checked: false },
      { value: "差分约束", checked: false },
      { value: "仙人掌", checked: false },
      { value: "二分图", checked: false },
      { value: "一般图的最大匹配", checked: false },
      { value: "上下界网络流", checked: false },
      { value: "最小割", checked: false },
      { value: "费用流", checked: false },
      { value: "圆方树", checked: false },
      { value: "Dilworth 定理", checked: false },
      { value: "弦图", checked: false },
      { value: "Floyd 算法", checked: false },
    ],
  },
  {
    title: "计算几何",
    tags: [
      { value: "三维计算几何", checked: false },
      { value: "向量", checked: false },
      { value: "凸包", checked: false },
      { value: "叉积", checked: false },
      { value: "线段相交", checked: false },
      { value: "半平面交", checked: false },
      { value: "扫描线", checked: false },
      { value: "旋转卡壳", checked: false },
      { value: "极角排序", checked: false },
      { value: "平面几何", checked: false },
    ],
  },
  {
    title: "树形数据结构",
    tags: [
      { value: "线段树", checked: false },
      { value: "并查集", checked: false },
      { value: "平衡树", checked: false },
      { value: "堆", checked: false },
      { value: "树状数组", checked: false },
      { value: "cdq 分治", checked: false },
      { value: "可并堆", checked: false },
      { value: "动态树 LCT", checked: false },
      { value: "树套树", checked: false },
      { value: "可持久化线段树", checked: false },
      { value: "可持久化整体二分", checked: false },
      { value: "K-D Tree", checked: false },
      { value: "李超线段树", checked: false },
      { value: "吉司机线段树", checked: false },
      { value: "segment tree beats", checked: false },
      { value: "线段树合并", checked: false },
    ],
  },
  {
    title: "博弈论",
    tags: [
      { value: "博弈树", checked: false },
      { value: "Nim", checked: false },
      { value: "SG 函数", checked: false },
    ],
  },
  {
    title: "线性数据结构",
    tags: [
      { value: "单调队列", checked: false },
      { value: "颜色段", checked: false },
      { value: "均摊（珂朵莉树 ODT）", checked: false },
      { value: "前缀和", checked: false },
      { value: "栈", checked: false },
      { value: "队列", checked: false },
      { value: "分块", checked: false },
      { value: "ST 表", checked: false },
      { value: "差分", checked: false },
      { value: "链表", checked: false },
      { value: "单调栈", checked: false },
      { value: "哈希表", checked: false },
    ],
  },
  {
    title: "多项式",
    tags: [
      { value: "快速傅里叶变换 FFT", checked: false },
      { value: "快速数论变换 NTT", checked: false },
      { value: "快速沃尔什变换 FWT", checked: false },
      { value: "快速莫比乌斯变换 FMT", checked: false },
      { value: "Berlekamp-Massey(BM) 算法", checked: false },
      { value: "集合幂级数", checked: false },
      { value: "子集卷积", checked: false },
    ],
  },
  {
    title: "数论",
    tags: [
      { value: "原根", checked: false },
      { value: "素数判断", checked: false },
      { value: "质数", checked: false },
      { value: "中国剩余定理", checked: false },
      { value: "欧几里得算法", checked: false },
      { value: "线性筛法", checked: false },
      { value: "Euler 定理", checked: false },
      { value: "莫比乌斯反演", checked: false },
      { value: "大数运算", checked: false },
    ],
  },
  {
    title: "经典密码学",
    tags: [
      { value: "单表替换", checked: false },
      { value: "周期置换密码（换位密码）", checked: false },
      { value: "维吉尼亚密码的加密解密", checked: false },
      { value: "HILL密码的加解密及破译", checked: false },
      { value: "Playfair的加解密及破译", checked: false },
    ],
  },
  {
    title: "序列密码",
    tags: [
      { value: "M序列的构造及破译", checked: false },
      { value: "非线性寄存器的构造实现", checked: false },
    ],
  },
  {
    title: "DES密码",
    tags: [
      { value: "验证DES弱密钥和半弱密钥", checked: false },
      { value: "基于DES的五种分组模式的实现", checked: false },
    ],
  },
  {
    title: "AES及背包试验",
    tags: [
      { value: "基于AES的五种分组模式的实现", checked: false },
      { value: "超背包密码加解密实现", checked: false },
    ],
  },
  {
    title: "RSA实现及安全分析",
    tags: [
      { value: "RSA加解密实现", checked: false },
      { value: "RSA选择密文攻击", checked: false },
      { value: "RSA共模攻击", checked: false },
      { value: "RSA小加密指数迭代攻击", checked: false },
      { value: "费马因子分解法实现", checked: false },
      { value: "Wiener低解密指数攻击", checked: false },
    ],
  },
  {
    title: "离散对数ELGamal型密码及其安全性分析",
    tags: [
      { value: "Elgamal加解密实验", checked: false },
      { value: "大步小步法攻击", checked: false },
    ],
  },
  {
    title: "ECC加解密",
    tags: [
      { value: "ECC的点乘运算实现", checked: false },
      { value: "ECC加解密流程", checked: false },
      { value: "明文嵌入方法（至少实现2种）", checked: false },
    ],
  },
  {
    title: "基于RSA的各类签名实现",
    tags: [
      { value: "RSA签名实现", checked: false },
      { value: "基于RSA的代理签名", checked: false },
      { value: "RSA有序多重签名", checked: false },
      { value: "基于RSA的强盲签名", checked: false },
    ],
  },
  {
    title: "基于ECC的各类签名实现",
    tags: [
      { value: "ECC数字签名", checked: false },
      { value: "基于ECC的代理签名", checked: false },
      { value: "ECC有序多重签名", checked: false },
    ],
  },
  {
    title: "基于离散对数的各类签名实现",
    tags: [
      { value: "Elgamal签名", checked: false },
      { value: "基于离散对数的盲签名", checked: false },
      { value: "Elgamal无序多重签名", checked: false },
      { value: "基于离散对数的群签名", checked: false },
      { value: "基于shamir的门限签名", checked: false },
    ],
  },
  {
    title: "零知识证明",
    tags: [
      { value: "基于RSA的零知识证明", checked: false },
      { value: "基于离散对数问题的零知识证明", checked: false },
    ],
  },
  {
    title: "哈希函数",
    tags: [
      { value: "MD4的攻击实验", checked: false },
      { value: "Hash应用于消息认证码", checked: false },
    ],
  },
]);
const customCheck = (
  tag: { value: string; checked: boolean },
  checked: boolean,
  ev: Event
) => {
  tag.checked = checked;
  props.handChange(tag, checked);
};
const customClose = (ev: Event, tag: { value: string; checked: boolean }) => {
  props.handDelete(tag);
};
/**
 * @description: 定义子组件接受参数
 */
const props = defineProps({
  tagsChose: {
    type: Array,
    default: () => [],
  },
  handChange: {
    type: Function,
    default: (tag: any, checked: boolean) => {
      console.log("checked被选中", tag);
    },
  },
  handDelete: {
    type: Function,
    default: (tag: any) => {
      console.log("tag被删除", tag);
    },
  },
  handSubmit: {
    type: Function,
    default: (tag: any) => {
      console.log("提交了傻瓜");
    },
  },
});

// 使用副本进行绑定
const localTags = ref([...props.tagsChose]);

// 监听父组件传递的tagsChose变化，更新副本
watch(
  () => props.tagsChose,
  (newTags) => {
    localTags.value = [...newTags];
  },
  { deep: true }
);
</script>
