<template>
  <div id="userRegisterView">
    <h2 style="margin-bottom: 16px">用户注册</h2>
    <a-form
      style="max-width: 480px; margin: 0 auto"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item
        field="userPassword"
        tooltip="密码不少于 8 位"
        label="再次输入密码"
      >
        <a-input-password
          v-model="form.checkPassword"
          placeholder="再次输入密码"
        />
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          html-type="submit"
          style="width: 120px; margin-right: 20px"
        >
          注册
        </a-button>
        <a-checkbox
          @change="handleRoleChange"
          @click="handleRoleClick"
          style="margin-right: 0%; text-align: left; width: 300px"
          :disabled="store.state.user.loginUser?.userRole !== 'admin'"
        >
          注册为管理员
        </a-checkbox>

        <a-button
          type="text"
          style="width: 290px; text-align: center"
          @click="handleLogin"
        >
          已有账号，去登录
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { UserControllerService, UserLoginRequest } from "../../../generated";
/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  userRole: "",
});

const router = useRouter();
const store = useStore();

/**
 * 提交表单
 * @param data
 */
const handleSubmit = async () => {
  if (form.userPassword.length < 8) {
    message.error("密码长度不能小于8位");
    return;
  }
  if (form.userPassword !== form.checkPassword) {
    message.error("两次输入的密码不一致");
    return;
  }
  const res = await UserControllerService.userRegisterUsingPost(form);
  if (res.code === 0) {
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    message.error("注册失败，" + res.message);
  }
};
const handleLogin = () => {
  router.push("/user/login");
};
const handleRoleChange = (e: any) => {
  form.userRole = e ? "admin" : "user";
  console.log(form.userRole);
};
const handleRoleClick = (e: any) => {
  if (store.state.user.loginUser?.userRole !== "admin") {
    e.preventDefault();
    message.error("您不是管理员");
  }
};
</script>
