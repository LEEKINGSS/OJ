<template>
  <div>
    <div class="TableData" style="background-color: white; text-align: center">
      <div>
        筛选条件
        <a-input
          :style="{ width: '320px', margin: '30px' }"
          placeholder="输入用户账号"
          allow-clear
          v-model="queryParm.userAccount"
        />
      </div>
      <div>
        角色
        <a-select
          v-model="queryParm.userRole"
          :style="{ width: 'auto' }"
          placeholder="选择角色"
          style="margin: 40px"
          allow-clear
        >
          <a-option value="user">普通用户</a-option>
          <a-option value="admin">管理员</a-option>
          <a-option value="ban">被封号</a-option>
        </a-select>

        <a-space>
          <a-button type="primary" @click="loadData">查询</a-button>
          <a-button type="outline" status="success" @click="openAddModal"
            >新增用户</a-button
          >
        </a-space>
      </div>
    </div>

    <div class="TableData">
      <a-table
        :show-header="true"
        :data="dataList"
        style="margin-top: 30px"
        :pagination="{
          total: total,
          showTotal: true,
          current: pageParm.current,
          pageSize: pageParm.pageSize,
        }"
        @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column title="用户">
            <template #cell="{ record }">
              <div class="message-container">
                <div class="avatar-wrapper">
                  <img
                    :src="
                      record.userAvatar ||
                      'https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f13fb09879ecb5185e440cef6eb9.png~tplv-uwbnlip3yd-webp.webp'
                    "
                    alt="头像"
                    class="avatar-image"
                  />
                </div>
                <div class="message-content">
                  <div class="name">{{ record.userAccount }}</div>
                  <div class="time">ID: {{ record.id }}</div>
                </div>
              </div>
            </template>
          </a-table-column>

          <a-table-column title="角色">
            <template #cell="{ record }">
              <a-tag :color="getRoleColor(record.userRole)">
                {{ record.userRole === "admin" ? "管理员" : "普通用户" }}
              </a-tag>
            </template>
          </a-table-column>

          <a-table-column title="创建时间">
            <template #cell="{ record }">
              {{ record.createTime }}
            </template>
          </a-table-column>

          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-space>
                <a-button size="mini" type="text" @click="openEditModal(record)"
                  >编辑</a-button
                >
                <a-popconfirm
                  content="确定要删除该用户吗?"
                  @ok="handleDelete(record)"
                >
                  <a-button size="mini" type="text" status="danger"
                    >删除</a-button
                  >
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:visible="modalVisible"
      :title="modalType === 'add' ? '新增用户' : '编辑用户'"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item field="id" label="id" v-if="modalType === 'edit'">
          <a-input v-model="form.id" :disabled="modalType === 'edit'" />
        </a-form-item>
        <a-form-item field="userAccount" label="账号">
          <a-input
            v-model="form.userAccount"
            placeholder="请输入账号"
            :disabled="modalType === 'edit'"
          />
        </a-form-item>

        <a-form-item
          field="userPassword"
          label="密码"
          v-if="modalType === 'add'"
        >
          <a-input-password
            v-model="form.userPassword"
            placeholder="请输入密码"
          />
        </a-form-item>

        <a-form-item field="userRole" label="角色">
          <a-select v-model="form.userRole" placeholder="请选择角色">
            <a-option value="user">普通用户</a-option>
            <a-option value="admin">管理员</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, reactive } from "vue";
import { Message } from "@arco-design/web-vue";
// 假设你生成的 API 路径如下，请根据实际生成的 Service 修改
import { UserControllerService, UserRegisterRequest } from "../../generated";

// --- 数据定义 ---
const queryParm = ref({
  userAccount: "",
  userRole: "",
  userName: "",
});

const pageParm = ref({
  current: 1,
  pageSize: 10,
});

const dataList = ref([]);
const total = ref(0);

// 弹窗控制
const modalVisible = ref(false);
const modalType = ref<"add" | "edit">("add");
const form = reactive({
  id: undefined,
  userAccount: "",
  userPassword: "",
  checkPassword: "", // 注册接口需要校验密码
  userRole: "user",
});

// --- 方法 ---

// 1. 加载数据 (Mock: 因为你的后端缺 list 接口，这里写的是预期的调用方式)
const loadData = async () => {
  // TODO: 需要在后端 UserController 添加 listUserByPage 接口
  const res = await UserControllerService.getAllUserUsingPost(queryParm.value);
  if (res.code === 0) {
    dataList.value = res.data;
  } else {
    Message.error("获取用户列表失败: " + res.message);
  }
  total.value = res.data.length;
};

// 2. 翻页
const handlePageChange = (page: number) => {
  pageParm.value.current = page;
};

// 3. 打开新增弹窗
const openAddModal = () => {
  modalType.value = "add";
  form.id = undefined;
  form.userAccount = "";
  form.userPassword = "";
  form.checkPassword = "";
  form.userRole = "user";
  modalVisible.value = true;
};

// 4. 打开编辑弹窗
const openEditModal = (record: any) => {
  modalType.value = "edit";
  form.id = record.id;
  form.userAccount = record.userAccount;
  // 编辑时不回显密码
  form.userRole = record.userRole;
  modalVisible.value = true;
};

// 5. 提交表单
const handleModalOk = async () => {
  if (modalType.value === "add") {
    // 调用现有的注册接口
    const registerRequest: UserRegisterRequest = {
      userAccount: form.userAccount,
      userPassword: form.userPassword,
      checkPassword: form.userPassword, // 自动填充确认密码
      userRole: form.userRole,
    };

    const res = await UserControllerService.userRegisterUsingPost(
      registerRequest
    );
    if (res.code === 0) {
      Message.success("添加成功");
      modalVisible.value = false;
      loadData();
    } else {
      Message.error("添加失败: " + res.message);
    }
  } else {
    const res = await UserControllerService.updateUserUsingPost(form);
    if (res.code === 0) {
      Message.success(res.data);
    }
    modalVisible.value = false;
    loadData();
  }
};

const handleModalCancel = () => {
  modalVisible.value = false;
};

// 6. 删除用户
const handleDelete = async (record: any) => {
  const res = await UserControllerService.deleteUserUsingPost({
    id: record.id,
  });
  if (res.code === 0) {
    Message.success("删除成功");
    loadData();
  }
};

// 辅助样式
const getRoleColor = (role: string) => {
  switch (role) {
    case "admin":
      return "red";
    case "user":
      return "blue";
    default:
      return "gray";
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
/* 完全复用你提供的 CSS */
.message-container {
  display: flex;
  align-items: flex-start;
  padding: 4px 0;
}

.avatar-wrapper {
  margin-right: 12px;
}

.avatar-image {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  margin-bottom: 2px;
}

.time {
  font-size: 12px;
  color: #999;
}

.TableData {
  width: 80%;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
