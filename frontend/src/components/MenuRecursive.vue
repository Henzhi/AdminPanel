<template>
  <template v-for="item in menus" :key="item.id">
    <!-- 含子菜单 -->
    <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path || String(item.id)">
      <template #title>
        <el-icon v-if="item.icon">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.name }}</span>
      </template>
      <menu-recursive :menus="item.children" />
    </el-sub-menu>

    <!-- 叶子菜单 -->
    <el-menu-item v-else :index="item.path" @click="handleClick(item)">
      <el-icon v-if="item.icon">
        <component :is="item.icon" />
      </el-icon>
      <template #title>{{ item.name }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineOptions({ name: 'MenuRecursive' })

const props = defineProps({
  menus: {
    type: Array,
    default: () => [],
  },
})

const router = useRouter()

function handleClick(item) {
  if (item.path) {
    router.push(item.path)
  }
}
</script>
