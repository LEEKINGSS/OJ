<template>
  <div class="page">
    <!-- 筛选区 -->
    <div class="filter-card">
      <div class="filter-title">筛选条件</div>

      <div class="filter-row">
        <div class="label">题目标题</div>
        <a-input
          v-model="name"
          placeholder="请输入题目标题"
          allow-clear
          class="w-320"
          @blur="updateData"
          @press-enter="updateData"
        />
      </div>

      <div class="filter-row">
        <div class="label">题目标签</div>
        <div class="tags-area">
          <tags-input
            :tagsChose="tagsChose"
            :handChange="handChange"
            :handDelete="handDelete"
            :handSubmit="updateData"
          />
        </div>
      </div>
    </div>

    <!-- 列表区 -->
    <div class="list-card">
      <a-tabs default-active-key="all">
        <a-tab-pane key="all" title="全部" />
      </a-tabs>

      <!-- 工具条（可按需扩展：学习状态/难度/关键字） -->
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="hint">
            共 <b>{{ totalRecords }}</b> 道题
          </div>
        </div>

        <div class="toolbar-right">
          <a-input
            v-model="keyword"
            allow-clear
            placeholder="搜索题目名称/简介/编号"
            class="w-320"
            @press-enter="onSearch"
            @clear="onSearch"
          >
            <template #suffix>
              <icon-search />
            </template>
          </a-input>
        </div>
      </div>

      <!-- 网格卡片 -->
      <a-spin :loading="loading" style="width: 100%">
        <div v-if="questions.length" class="grid">
          <div v-for="q in questions" :key="q.id" class="grid-item">
            <a-card class="q-card" :bordered="false" :hoverable="true">
              <template #cover>
                <div
                  class="cover"
                  :style="{ backgroundImage: `url(${getCover(q)})` }"
                >
                  <div class="cover-top">
                    <span class="cover-id">编号：{{ q.id }}</span>
                  </div>
                  <div class="cover-mid">
                    <div class="cover-title">题目</div>
                    <div class="cover-sub">question</div>
                  </div>
                </div>
              </template>

              <div class="card-body">
                <router-link
                  class="q-title"
                  :to="{ name: 'questionDetail', params: { id: q.id } }"
                >
                  {{ q.title }}
                </router-link>

                <div class="meta">
                  <div class="meta-line" v-if="q.submitNum">
                    <span class="meta-label">提交</span>
                    <a-tag color="#fadb14">{{ q.submitNum ?? 0 }}</a-tag>

                    <span class="meta-label" style="margin-left: 10px"
                      >通过</span
                    >
                    <a-tag color="#52c41a">{{ q.acceptedNum ?? 0 }}</a-tag>
                  </div>

                  <div class="meta-line" v-if="q.submitNum">
                    <span class="meta-label">通过率</span>
                    <a-tooltip :content="acceptRateText(q)">
                      <a-progress
                        :percent="acceptRate(q)"
                        :show-text="false"
                        style="flex: 1"
                      />
                    </a-tooltip>
                    <span class="rate-text">{{ acceptRateText(q) }}</span>
                  </div>

                  <div class="tags" v-if="parsedTags(q).length">
                    <a-tag
                      v-for="(t, idx) in parsedTags(q)"
                      :key="idx"
                      color="#13c2c2"
                      class="tag"
                    >
                      {{ t }}
                    </a-tag>
                  </div>
                </div>
              </div>

              <template #actions>
                <a-button type="text" class="go" @click="goDetail(q.id)">
                  进入 <icon-right />
                </a-button>
              </template>
            </a-card>
          </div>
        </div>

        <a-empty v-else description="暂无数据" />
      </a-spin>

      <!-- 分页 -->
      <div class="pager">
        <a-pagination
          :total="totalRecords"
          :current="currentPage"
          :page-size="pageSize"
          show-total
          show-jumper
          show-page-size
          @change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { QuestionControllerService } from "../../../generated";
import TagsInput from "@/components/TagsInput.vue";
import { IconSearch, IconRight } from "@arco-design/web-vue/es/icon";

const router = useRouter();

const loading = ref(false);

const name = ref("");
const keyword = ref(""); // 额外的搜索框（按需使用）
const tags = ref("");

const totalRecords = ref(12);
const totalPages = ref(0);
const currentPage = ref(1);
const pageSize = ref(12); // 网格卡片通常一页 12/16 更顺眼

const questions = ref<any[]>([
  {
    id: 1,
    title: "经典密码学",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/%E7%BB%8F%E5%85%B8%E5%AF%86%E7%A0%81%E5%AD%A6.jpg",
  },
  {
    id: 2,
    title: "序列密码",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/%E5%BA%8F%E5%88%97%E5%AF%86%E7%A0%81.jpg",
  },
  {
    id: 3,
    title: "DES密码",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/DES%E5%AF%86%E7%A0%81.png",
  },
  {
    id: 4,
    title: "AES及背包试验",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/AES%E5%8F%8A%E8%83%8C%E5%8C%85%E8%AF%95%E9%AA%8C.jpg",
  },
  {
    id: 5,
    title: "RSA实现及安全分析",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/RSA%E5%AE%9E%E7%8E%B0%E5%8F%8A%E5%AE%89%E5%85%A8%E5%88%86%E6%9E%90.jpg",
  },
  {
    id: 6,
    title: "离散对数ELGamal型密码及其安全性分析",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/ELGamal%E5%9E%8B%E5%AF%86%E7%A0%81.jpg",
  },
  {
    id: 42,
    title: "ECC加解密",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/ECC%E5%8A%A0%E8%A7%A3%E5%AF%86.jpg",
  },
  {
    id: 8,
    title: "基于RSA的各类签名实现",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/%E5%9F%BA%E4%BA%8ERSA%E7%9A%84%E5%90%84%E7%B1%BB%E7%AD%BE%E5%90%8D%E5%AE%9E%E7%8E%B0.jpg",
  },
  {
    id: 9,
    title: "基于ECC的各类签名实现",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/%E5%9F%BA%E4%BA%8EECC%E7%9A%84%E5%90%84%E7%B1%BB%E7%AD%BE%E5%90%8D%E5%AE%9E%E7%8E%B0.jpg",
  },
  {
    id: 10,
    title: "基于离散对数的各类签名实现",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/%E5%9F%BA%E4%BA%8E%E7%A6%BB%E6%95%A3%E5%AF%B9%E6%95%B0%E7%9A%84%E5%90%84%E7%B1%BB%E7%AD%BE%E5%90%8D%E5%AE%9E%E7%8E%B0.jpg",
  },
  {
    id: 11,
    title: "零知识证明协议",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/%E9%9B%B6%E7%9F%A5%E8%AF%86%E8%AF%81%E6%98%8E%E5%8D%8F%E8%AE%AE.jpg",
  },
  {
    id: 12,
    title: "Hash函数",
    coverUrl:
      "https://liking.oss-cn-wulanchabu.aliyuncs.com/%E5%AF%86%E7%A0%81%E5%AD%A6/Hash%E5%87%BD%E6%95%B0.jpg",
  },
]);

/** 拉取数据（统一入口，避免重复代码） */
const fetchQuestions = async () => {
  loading.value = true;
  try {
    // 你原接口签名：listQuestionUsingGet(name, currentPage, pageSize, tags)
    // 这里把 keyword 合并到 name（如果你后端有单独参数，就改成对应字段）
    const finalName =
      (keyword.value?.trim() ? keyword.value.trim() : name.value) ?? "";
    const res = await QuestionControllerService.listQuestionUsingGet(
      finalName,
      currentPage.value,
      pageSize.value,
      tags.value
    );

    if (res.code === 0) {
      questions.value = res.data?.questions ?? [];
      totalRecords.value = res.data?.totalRecords ?? 0;
      totalPages.value = res.data?.totalPages ?? 0;
      pageSize.value = res.data?.pageSize ?? pageSize.value;
      currentPage.value = res.data?.currentPage ?? currentPage.value;
    }
  } finally {
    loading.value = false;
  }
};

// onMounted(fetchQuestions);

const updateData = async () => {
  currentPage.value = 1;
  await fetchQuestions();
};

const onSearch = async () => {
  currentPage.value = 1;
  await fetchQuestions();
};

const handlePageChange = async (page: number) => {
  currentPage.value = page;
  await fetchQuestions();
};

const handlePageSizeChange = async (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  await fetchQuestions();
};

/** 跳转详情 */
const goDetail = (id: number | string) => {
  router.push({ name: "questionDetail", params: { id } });
};

/** 通过率 */
const acceptRate = (q: any) => {
  const s = Number(q?.submitNum ?? 0);
  const a = Number(q?.acceptedNum ?? 0);
  if (!s) return 0;
  return Math.max(0, Math.min(1, a / s));
};

const acceptRateText = (q: any) => {
  const p = acceptRate(q) * 100;
  return `${p.toFixed(2)}%`;
};

/** 解析 tags（你原来是 JSON.parse(record.tags)） */
const parsedTags = (q: any): string[] => {
  if (!q?.tags) return [];
  try {
    const arr = typeof q.tags === "string" ? JSON.parse(q.tags) : q.tags;
    return Array.isArray(arr) ? arr : [];
  } catch {
    // 如果后端改成了 "a,b,c" 也能兼容
    if (typeof q.tags === "string")
      return q.tags
        .split(",")
        .map((x: string) => x.trim())
        .filter(Boolean);
    return [];
  }
};

/** 封面：如果你有真实封面字段，把这里替换成 q.coverUrl 等 */
const getCover = (q: any) => {
  // 用 data-uri 做渐变封面，占位也不会闪烁
  return (
    q?.coverUrl ||
    "data:image/svg+xml;charset=utf-8," +
      encodeURIComponent(`
      <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="600">
        <defs>
          <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stop-color="#7dd3fc"/>
            <stop offset="60%" stop-color="#60a5fa"/>
            <stop offset="100%" stop-color="#a78bfa"/>
          </linearGradient>
        </defs>
        <rect width="1200" height="600" fill="url(#g)"/>
        <circle cx="160" cy="120" r="120" fill="rgba(255,255,255,0.12)"/>
        <circle cx="1020" cy="520" r="200" fill="rgba(255,255,255,0.10)"/>
      </svg>
    `)
  );
};

/**
 * 标签选择逻辑（保留你原实现）
 */
const tagsChose = ref<any[]>([]);
const handChange = (
  tag: { value: string; checked: boolean },
  checked: boolean
) => {
  if (checked) {
    tagsChose.value.push(tag);
  } else {
    tagsChose.value = tagsChose.value.filter(
      (item) => item.value !== tag.value
    );
  }
};

const handDelete = (tag: { value: string; checked: boolean }) => {
  tagsChose.value = tagsChose.value.filter((item) => item.value !== tag.value);
  tag.checked = false;
};

watch(
  () => tagsChose.value,
  (newTags) => {
    const tagValues = newTags.map(
      (item: { value: string; checked: boolean }) => item.value
    );
    tags.value = tagValues.join(",");
  },
  { deep: true }
);
</script>

<style scoped>
.page {
  padding: 18px;
}

.filter-card,
.list-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.filter-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 14px;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}

.label {
  width: 90px;
  color: #4e5969;
}

.tags-area {
  flex: 1;
  min-width: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 0 18px;
  gap: 12px;
}

.hint {
  color: #4e5969;
}

.w-320 {
  width: 320px;
}

/* 网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 1200px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 720px) {
  .grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

.q-card {
  border-radius: 12px;
  overflow: hidden;
}

.cover {
  height: 160px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.cover-top {
  position: absolute;
  top: 10px;
  right: 10px;
}

.cover-id {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(6px);
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.cover-mid {
  position: absolute;
  left: 18px;
  bottom: 16px;
  color: #fff;
}

.cover-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
}
.cover-sub {
  opacity: 0.9;
  margin-top: 2px;
}

.card-body {
  padding: 4px 2px 2px;
}

.q-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  text-decoration: none;
  line-height: 1.4;
  margin-bottom: 10px;

  /* 两行省略 */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta {
  color: #4e5969;
  font-size: 13px;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.meta-label {
  white-space: nowrap;
}

.rate-text {
  min-width: 54px;
  text-align: right;
  color: #1d2129;
  font-weight: 500;
}

.tags {
  margin-top: 6px;
}

.tag {
  margin-right: 6px;
  margin-bottom: 6px;
}

.go {
  width: 100%;
  justify-content: center;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}
</style>
