<template>
  <div class="dashboard">
    <n-grid cols="1 s:2 m:3 l:4" responsive="screen" :x-gap="16" :y-gap="16">
      <n-grid-item>
        <n-card>
          <n-statistic label="总用户数" :value="statistics.userCount">
            <template #prefix>
              <n-icon :component="PeopleOutline" />
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      
      <n-grid-item>
        <n-card>
          <n-statistic label="角色数" :value="statistics.roleCount">
            <template #prefix>
              <n-icon :component="ShieldOutline" />
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      
      <n-grid-item>
        <n-card>
          <n-statistic label="菜单数" :value="statistics.menuCount">
            <template #prefix>
              <n-icon :component="MenuOutline" />
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      
      <n-grid-item>
        <n-card>
          <n-statistic label="在线用户" :value="statistics.onlineCount">
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="欢迎使用管理系统" style="margin-top: 16px">
      <p>这是一个基于Vue 3 + Naive UI + Spring Boot + Jimmer + Sa-Token的后台管理系统。</p>
      <p>主要功能包括：</p>
      <ul>
        <li>用户管理：用户的增删改查</li>
        <li>角色管理：角色权限配置</li>
        <li>菜单管理：动态菜单配置</li>
        <li>权限控制：基于Sa-Token的权限控制</li>
      </ul>
    </n-card>
  </div>
</template>

<script>
export default {
  name: 'Dashboard'
}
</script>

<script setup>
import { ref, onMounted } from 'vue'
import { PeopleOutline, ShieldOutline, MenuOutline, PersonOutline } from '@vicons/ionicons5'

const statistics = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  onlineCount: 1
})

onMounted(() => {
  // 这里可以调用API获取统计数据
  statistics.value = {
    userCount: 25,
    roleCount: 5,
    menuCount: 12,
    onlineCount: 3
  }
})
</script>

<style scoped>
.dashboard {
  padding: 16px;
}
</style>