/*     */ package me.mangorage.graves2.API;
/*     */ 
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.inventory.Inventory;
/*     */ import org.bukkit.inventory.ItemStack;
/*     */ import org.bukkit.util.io.BukkitObjectInputStream;
/*     */ import org.bukkit.util.io.BukkitObjectOutputStream;
/*     */ import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Serializer
/*     */ {
/*     */   public static String[] playerInventoryToBase64(Inventory playerInventory) throws IllegalStateException {
/*  25 */     String content = toBase64(playerInventory);
/*  26 */     String armor = "";
/*     */     
/*  28 */     return new String[] { content, armor };
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
/*     */     try {
/*  45 */       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
/*  46 */       BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
/*     */ 
/*     */       
/*  49 */       dataOutput.writeInt(items.length);
/*     */ 
/*     */       
/*  52 */       for (int i = 0; i < items.length; i++) {
/*  53 */         dataOutput.writeObject(items[i]);
/*     */       }
/*     */ 
/*     */       
/*  57 */       dataOutput.close();
/*  58 */       return Base64Coder.encodeLines(outputStream.toByteArray());
/*  59 */     } catch (Exception e) {
/*  60 */       throw new IllegalStateException("Unable to save item stacks.", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String toBase64(Inventory inventory) throws IllegalStateException {
/*     */     try {
/*  80 */       ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
/*  81 */       BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
/*     */ 
/*     */       
/*  84 */       dataOutput.writeInt(inventory.getSize());
/*     */ 
/*     */       
/*  87 */       for (int i = 0; i < inventory.getSize(); i++) {
/*  88 */         dataOutput.writeObject(inventory.getItem(i));
/*     */       }
/*     */ 
/*     */       
/*  92 */       dataOutput.close();
/*  93 */       return Base64Coder.encodeLines(outputStream.toByteArray());
/*  94 */     } catch (Exception e) {
/*  95 */       throw new IllegalStateException("Unable to save item stacks.", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Inventory fromBase64(String data) throws IOException {
/*     */     try {
/* 116 */       ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
/* 117 */       BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
/* 118 */       Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());
/* 119 */
/*     */       
/* 121 */       for (int i = 0; i < inventory.getSize(); i++) {
/* 122 */         inventory.setItem(i, (ItemStack)dataInput.readObject());
/*     */       }
/*     */       
/* 125 */       dataInput.close();
/* 126 */       return inventory;
/* 127 */     } catch (ClassNotFoundException e) {
/* 128 */       throw new IOException("Unable to decode class type.", e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
/*     */     try {
/* 145 */       ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
/* 146 */       BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
/* 147 */       ItemStack[] items = new ItemStack[dataInput.readInt()];
/*     */ 
/*     */       
/* 150 */       for (int i = 0; i < items.length; i++) {
/* 151 */         items[i] = (ItemStack)dataInput.readObject();
/*     */       }
/*     */       
/* 154 */       dataInput.close();
/* 155 */       return items;
/* 156 */     } catch (ClassNotFoundException e) {
/* 157 */       throw new IOException("Unable to decode class type.", e);
/*     */     } 
/*     */   }
/*     */ }


