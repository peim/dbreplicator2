package ru.taximaxim.dbreplicator2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Персистентный класс колонки таблицы
 * 
 * @author volodin_aa
 *
 */
@Entity
@Table(name = "required_columns_table")
public class RequiredColumnsTableModel {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id_required_columns_table")
    private Integer id;

    /**
     * Получение идентификатора
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * Установка идентификатора
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Название колонки
     */
    @Column(name = "column_name")
    private String columnName;

    /**
     * Получение название колонки
     * @return
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Получение название колонки
     * @param columnName
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Игнорируемая колонка, принадлежащей таблицы
     */
    @ManyToOne
    @JoinColumn(name = "id_table")
    private TableModel table;

    /**
     * @see TableModel#table
     */
    public TableModel getTable() {
        return this.table;
    }

    /**
     * @see TableModel#table
     */
    public void setTable(TableModel table) {
        this.table = table;
    }

}