<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模板名称" prop="typeName">
        <el-input
          v-model="queryParams.typeName"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建人" prop="createUser">
        <el-input
          v-model="queryParams.createUser"
          placeholder="请输入创建人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="更新人" prop="updateUser">
        <el-input
          v-model="queryParams.updateUser"
          placeholder="请输入更新人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['system:template:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:template:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:template:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:template:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" />
      <el-table-column label="模版类型" align="center" prop="type" />
      <el-table-column label="模板名称" align="center" prop="typeName" />
      <el-table-column label="创建人" align="center" prop="createUser" />
      <el-table-column label="更新人" align="center" prop="updateUser" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:template:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:template:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="90%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 模板类型 -->
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择模板类型">
            <el-option
              v-for="item in types"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <!-- 子表单列表 -->
        <div
          v-for="(item, index) in form.children"
          :key="index"
          style="border: 1px solid #eee; padding: 10px; margin-bottom: 10px; border-radius: 4px;"
        >
          <el-row :gutter="10" type="flex" align="middle">
            <el-col :span="4">
              <el-form-item
                label="编码"
                :label-width="'50px'"
                :prop="'children.' + index + '.code'"
                :rules="[{ required: true, message: '请输入编码' }]"
              >
                <el-input v-model="item.code" placeholder="编码" />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item
                label="名称"
                :label-width="'50px'"
                :prop="'children.' + index + '.name'"
                :rules="[{ required: true, message: '请输入名称' }]"
              >
                <el-input v-model="item.name" placeholder="名称" />
              </el-form-item>
            </el-col>

            <el-col :span="4">
              <el-form-item label="校验权限" :label-width="'75px'">
                <el-switch v-model="item.checkPermission" active-text="" inactive-text="" />
              </el-form-item>
            </el-col>

            <el-col :span="5">
              <el-form-item
                label="权限码"
                :label-width="'60px'"
                :prop="'children.' + index + '.permissionCode'"
                :rules="item.checkPermission ? [{ required: true, message: '请输入权限码' }] : []"
              >
                <el-input v-model="item.permissionCode" placeholder="权限码" />
              </el-form-item>
            </el-col>

            <el-col :span="3">
              <el-form-item
                label="排序"
                :label-width="'50px'"
                :prop="'children.' + index + '.sort'"
                :rules="[{ required: true, message: '请输入排序' }]"
              >
                <el-input-number v-model="item.sort" :min="1" style="width: 100%;" />
              </el-form-item>
            </el-col>

            <el-col :span="4" style="text-align: right;">
              <el-button type="danger" icon="el-icon-delete" @click="removeChild(index)">
                删除
              </el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 添加按钮 -->
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" @click="addChild">新增子项</el-button>
        </el-form-item>
      </el-form>

      <!-- 弹窗底部 -->
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTemplate, getTemplate, delTemplate, addTemplate, updateTemplate } from "@/api/system/template";

export default {
  name: "Template",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 考核模板表格数据
      templateList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        type: null,
        typeName: null,
        createUser: null,
        updateUser: null
      },
      // 表单参数
      form: {
        type: '',
        children: []
      },
      // 表单校验
      rules: {
        type: [
          { required: true, message: "模版类型不能为空", trigger: "change" }
        ],
        typeName: [
          { required: true, message: "模板名称不能为空", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        updateTime: [
          { required: true, message: "$comment不能为空", trigger: "blur" }
        ],
        createUser: [
          { required: true, message: "创建人不能为空", trigger: "blur" }
        ],
        updateUser: [
          { required: true, message: "更新人不能为空", trigger: "blur" }
        ]
      },
      types:[
        { label: '重庆市大足区龙岗幼儿园教师月绩效考核细则', value: 0},
        { label: '重庆市大足区龙岗幼儿园保育教师月绩效考核细则', value: 1},
        { label: '重庆市大足区龙岗幼儿园食堂人员月绩效考核细则', value: 2},
        { label: '重庆市大足区龙岗幼儿园保安人员工作考核评细则', value: 3},
        { label: '重庆市大足区龙岗幼儿园学年度师德师风考核评价表', value: 4}
      ]
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询考核模板列表 */
    getList() {
      this.loading = true;
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        type: null,
        typeName: null,
        createTime: null,
        updateTime: null,
        createUser: null,
        updateUser: null,
        children: []
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加考核模板";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getTemplate(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改考核模板";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$nextTick(() => {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateTemplate(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addTemplate(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
            }
          }
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除考核模板编号为"' + ids + '"的数据项？').then(function() {
        return delTemplate(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/template/export', {
        ...this.queryParams
      }, `template_${new Date().getTime()}.xlsx`)
    },
    addChild() {
      this.form.children.push({
        code: '',
        name: '',
        checkPermission: false,
        permissionCode: '',
        sort: 1
      });
    },
    removeChild(index) {
      this.form.children.splice(index, 1);
    },
  }
};
</script>
